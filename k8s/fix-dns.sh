#!/usr/bin/env bash
# Фикс DNS кластера minikube для сетей с VPN/fake-DNS.

# Использование:
#   ./k8s/fix-dns.sh            применить фикс + проверить
#   ./k8s/fix-dns.sh --check    только проверить текущее состояние
#   ./k8s/fix-dns.sh --revert   вернуть исходный forward на нодный резолвер
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOT_BLOCK='forward . tls://1.1.1.1 {
           max_concurrent 1000
           tls_servername cloudflare-dns.com
           health_check 10s
        }'
ORIG_BLOCK='forward . /etc/resolv.conf {
           max_concurrent 1000
        }'

need() { command -v "$1" >/dev/null || { echo "нет утилиты: $1"; exit 1; }; }
need kubectl; need python3

check() {
  if ! kubectl get nodes >/dev/null 2>&1; then
    echo "FAIL: кластер недоступен для kubectl (запущен ли minikube? пуст ли ~/.kube/config?)."
    echo "Подсказка: 'minikube start' пересоздаёт kubeconfig и поднимает существующую VM."
    return 1
  fi
  echo "Проверка DNS из пода (nslookup api.github.com):"
  kubectl delete pod dns-fix-check --ignore-not-found --force --grace-period=0 >/dev/null 2>&1 || true
  if ! kubectl run dns-fix-check --restart=Never --image=busybox:1.36 -- nslookup api.github.com >/dev/null 2>&1; then
    echo "FAIL: не удалось запустить проверочный под busybox:1.36."
    return 1
  fi
  local phase="" out
  for _ in $(seq 1 60); do
    phase=$(kubectl get pod dns-fix-check -o jsonpath='{.status.phase}' 2>/dev/null || echo "")
    [ "$phase" = "Succeeded" ] || [ "$phase" = "Failed" ] && break
    sleep 1
  done
  out=$(kubectl logs dns-fix-check 2>/dev/null || true)
  kubectl delete pod dns-fix-check --ignore-not-found --force --grace-period=0 >/dev/null 2>&1 || true
  echo "$out" | sed 's/^/    /'
  if [ -z "$out" ]; then
    echo "FAIL: проверочный под не дал логов (phase=${phase:-unknown}) — образ busybox:1.36 есть в VM?"
    return 1
  fi
  if echo "$out" | grep -qE 'aet\.spotify\.com|144\.31\.'; then
    echo "FAIL: ответ подменён — внешние DNS ходят мимо VPN-туннеля."
    return 1
  fi
  if ! echo "$out" | grep -qE 'Address:? [0-9]+\.'; then
    echo "FAIL: разрешение не работает вовсе."
    return 1
  fi
  echo "OK: api.github.com резолвится в настоящий адрес."
}

dump_cm() { kubectl -n kube-system get cm coredns -o yaml; }

patch_corefile() { # $1=regex  $2=replacement
  local tmp; tmp=$(mktemp)
  dump_cm > "$tmp"
  python3 - "$tmp" "$1" "$2" <<'PY'
import re, sys
path, pattern, repl = sys.argv[1], sys.argv[2], sys.argv[3]
src = open(path).read()
new, n = re.subn(pattern, repl, src, count=1, flags=re.S)
if n != 1:
    sys.exit("блок forward в Corefile не найден — Corefile нестандартный?")
open(path, "w").write(new)
PY
  kubectl apply -f "$tmp" >/dev/null
  kubectl -n kube-system rollout restart deployment/coredns >/dev/null
  kubectl -n kube-system rollout status deployment/coredns --timeout=120s
  # endpoints Service нацеливаются на новый под не мгновенно; даём DNS устаканиться
  sleep 3
  rm -f "$tmp"
}

case "${1:-apply}" in
  --check)
    check
    ;;
  --revert)
    echo "Возвращаю forward на резолвер ноды..."
    patch_corefile 'forward \. tls://1\.1\.1\.1 \{[^}]*\}' "$ORIG_BLOCK"
    echo "Готово (состояние до фикса)."
    check || true
    ;;
  apply)
    if dump_cm | grep -q 'tls://1.1.1.1'; then
      echo "Фикс уже применён. $(check || true)"
      exit 0
    fi
    echo "Сохраняю резервную копию текущего ConfigMap..."
    ts=$(date +%Y%m%d-%H%M%S)
    dump_cm > "$SCRIPT_DIR/coredns-backup-$ts.yaml"
    echo "  -> $SCRIPT_DIR/coredns-backup-$ts.yaml"
    echo "Переключаю внешние DNS CoreDNS на DNS-over-TLS (1.1.1.1:853)..."
    patch_corefile 'forward \. /etc/resolv\.conf \{[^}]*\}' "$DOT_BLOCK"
    echo "Фикс применён."
    check
    ;;
  *)
    echo "usage: $0 [--check|--revert]"; exit 1
    ;;
esac
