#!/usr/bin/env python3
"""Генератор нагрузки для веб-части Link Tracker (scrapper + frontend).

Только стандартная библиотека Python 3 (без зависимостей).

Что делает:
  - регистрирует N пользователей (POST /auth/register),
  - логинится под каждым (POST /auth/login),
  - подписывает каждого на рабочие ссылки (по умолчанию
    https://github.com/psECode/LinkTracker и https://github.com/psECode/PermGuba),
  - крутит цикл только по «простым» ручкам: GET /api/links,
    GET /api/notifications, GET /auth/me, POST /api/notifications/read
    и редкие успешные переподписки remove→add,
  - ошибки (401/404/409/400) генерируются ТОЛЬКО в chaos-режиме (--chaos),
  - собирает latency и статус-коды по каждой ручке,
  - печатает сводную таблицу (эндпоинт, rps, p50/p95/p99, ошибки, коды).

Собранные этими запросами метрики (http_server_requests_seconds, api_requests_total,
auth_events_total, links_tracked_total) отдаются scrapper-ом на /metrics:8012 и
скрейпятся VictoriaMetrics — их можно смотреть на дашбордах SLI/SLO.

Примеры:
  # типовой запуск против фронтенда в minikube (без ошибок)
  ./scripts/loadgen.py --base-url http://$(minikube ip):30080 --users 5 --rps 20 --duration 10m

  # направить напрямую в scrapper (kubectl port-forward svc/scrapper 8081:8081)
  ./scripts/loadgen.py --base-url http://localhost:8081 --users 3 --rps 10 --duration 5m

  # свои ссылки для отслеживания
  ./scripts/loadgen.py --link https://github.com/psECode/LinkTracker --link https://github.com/psECode/PermGuba

  # перемешать успешные и неуспешные сценарии (для доли ошибок на графиках)
  ./scripts/loadgen.py --base-url http://$(minikube ip):30080 --chaos --chaos-ratio 0.2

  # убрать все ссылки в конце и завершиться
  ./scripts/loadgen.py --base-url http://localhost:30080 --cleanup
"""

from __future__ import annotations

import argparse
import json
import random
import re
import string
import sys
import threading
import time
import urllib.error
import urllib.request
from collections import defaultdict  # noqa: F401  (может пригодиться для группировки)
from dataclasses import dataclass, field

# ---------------------------------------------------------------- данные

# Рабочие ссылки: только реальные репозитории (добавление/удаление своих
# подписок всегда проходит успешно, без 404/409 в штатном режиме).
FIX_LINKS = [
    "https://github.com/psECode/LinkTracker",
    "https://github.com/psECode/PermGuba",
]

# Ссылки только для chaos-сценариев (гарантированные ошибки 404/409).
SO_LINKS = ["https://stackoverflow.com/questions/{id}/how-to-do-x-in-y"]


def random_email(rng: random.Random, session_id: str) -> str:
    user = "loadgen-" + session_id + "-" + "".join(rng.choices(string.ascii_lowercase + string.digits, k=6))
    return f"{user}@loadgen.local"


def random_link(rng: random.Random) -> str:
    """Ссылка, которой у пользователя точно нет — только для chaos."""
    return rng.choice(SO_LINKS).format(id=rng.randint(1, 10_000_000))


# ---------------------------------------------------------------- http

@dataclass
class Stats:
    counts: int = 0
    errors: int = 0
    latencies: list[float] = field(default_factory=list)  # seconds
    statuses: dict[int, int] = field(default_factory=dict)
    lock: threading.Lock = field(default_factory=threading.Lock)

    def record(self, status: int, latency: float) -> None:
        with self.lock:
            self.counts += 1
            self.latencies.append(latency)
            self.statuses[status] = self.statuses.get(status, 0) + 1
            if status >= 400 or status == 0:
                self.errors += 1


class LoadGen:
    def __init__(self, args: argparse.Namespace) -> None:
        self.base = args.base_url.rstrip("/")
        self.users = args.users
        self.rps = args.rps
        self.duration = args.duration
        self.chaos = args.chaos
        self.chaos_ratio = args.chaos_ratio
        self.cleanup = args.cleanup
        self.links_to_track = list(args.link) if args.link else list(FIX_LINKS)
        self.stop = threading.Event()
        self.stats: dict[str, Stats] = {}
        self.stats_lock = threading.Lock()
        self.sessions: list[dict] = []  # {email, token, links}
        self.sessions_lock = threading.Lock()
        self.rng = random.Random(args.seed)
        self.session_id = time.strftime("%Y%m%d%H%M%S")

    def stat(self, name: str) -> Stats:
        with self.stats_lock:
            if name not in self.stats:
                self.stats[name] = Stats()
            return self.stats[name]

    # --- запросы

    def request(self, name: str, method: str, path: str, token: str | None, body: dict | None = None) -> tuple[int, str]:
        headers = {"Accept": "application/json"}
        if token:
            headers["Authorization"] = f"Bearer {token}"
        data = None
        if body is not None:
            headers["Content-Type"] = "application/json"
            data = json.dumps(body).encode()
        req = urllib.request.Request(self.base + path, data=data, headers=headers, method=method)
        start = time.monotonic()
        status = 0
        payload = ""
        try:
            with urllib.request.urlopen(req, timeout=15) as resp:
                status = resp.status
                payload = resp.read().decode(errors="replace")
        except urllib.error.HTTPError as e:
            status = e.code
            payload = e.read().decode(errors="replace")
        except Exception:
            status = 0
        self.stat(name).record(status, time.monotonic() - start)
        return status, payload

    def expect_status(self, name: str, method: str, path: str, token: str | None, want: int, body: dict | None = None) -> tuple[int, str]:
        status, payload = self.request(name, method, path, token, body)
        if status == 0 and want != 0:
            # network error: retry once to keep the flow alive
            status, payload = self.request(name, method, path, token, body)
        return status, payload

    # --- сценарии

    def register(self, email: str) -> str:
        status, _ = self.expect_status(
            "register", "POST", "/auth/register", None, 201, {"email": email, "password": "loadgen-pass-123"})
        if status == 409:
            # пользователь уже существует (повторный запуск) — это не ошибка сценария
            pass
        return email

    def login(self, email: str) -> str:
        status, payload = self.expect_status(
            "login", "POST", "/auth/login", None, 200, {"email": email, "password": "loadgen-pass-123"})
        if status != 200:
            return ""
        try:
            return json.loads(payload)["accessToken"]
        except (KeyError, ValueError):
            return ""

    def me(self, token: str) -> None:
        self.expect_status("me", "GET", "/auth/me", token, 200)

    def links(self, token: str) -> None:
        self.expect_status("links.list", "GET", "/api/links", token, 200)

    def add_link(self, token: str, url: str) -> None:
        self.expect_status("links.add", "POST", "/api/links", token, 200, {"link": url, "tags": ["loadgen"]})

    def remove_link(self, token: str, url: str) -> None:
        self.expect_status("links.remove", "DELETE", "/api/links", token, 200, {"link": url})

    def tracked_links(self, token: str) -> list[str]:
        """Список URL, которые уже отслеживает пользователь."""
        status, payload = self.expect_status("links.list", "GET", "/api/links", token, 200)
        if status != 200:
            return []
        try:
            return [item.get("url", "") for item in json.loads(payload).get("links", [])]
        except (KeyError, ValueError):
            return []

    def ensure_links(self, token: str) -> None:
        """Добавить рабочие ссылки (только отсутствующие — без 409)."""
        tracked = set(self.tracked_links(token))
        for url in self.links_to_track:
            if url not in tracked:
                self.add_link(token, url)

    def notifications(self, token: str) -> None:
        self.expect_status("notifications.list", "GET", "/api/notifications?unread=false&limit=10", token, 200)

    def mark_read(self, token: str) -> None:
        self.expect_status("notifications.read", "POST", "/api/notifications/read", token, 200, {"ids": [], "all": True})

    # --- рабочие потоки

    def worker(self, worker_id: int) -> None:
        rng = random.Random(self.rng.random() * 1e9 + worker_id)
        email = random_email(rng, self.session_id)
        self.register(email)
        token = self.login(email)
        if not token:
            token = self.login(email)  # одна повторная попытка
        if not token:
            return

        # подписки на рабочие ссылки — один раз, без ошибок в штатном режиме
        self.ensure_links(token)

        with self.sessions_lock:
            self.sessions.append({"email": email, "token": token})

        interval = 1.0 / max(self.rps / max(self.users, 1), 0.01)
        next_at = time.monotonic() + rng.uniform(0, interval)
        while not self.stop.is_set():
            now = time.monotonic()
            if now < next_at:
                time.sleep(min(next_at - now, 0.05))
                continue
            next_at += interval
            bad = self.chaos and rng.random() < self.chaos_ratio
            action = rng.random()
            try:
                if bad:
                    self.negative_step(token, rng)
                elif action < 0.40:
                    self.links(token)
                elif action < 0.70:
                    self.notifications(token)
                elif action < 0.80:
                    self.me(token)
                elif action < 0.90:
                    self.mark_read(token)
                else:
                    # переподписка на рабочую ссылку: remove → add, обе операции успешны
                    url = rng.choice(self.links_to_track)
                    self.remove_link(token, url)
                    self.add_link(token, url)
            except Exception as e:  # не роняем поток
                print(f"[worker-{worker_id}] ошибка сценария: {e}", file=sys.stderr)

    def negative_step(self, token: str, rng: random.Random) -> None:
        """Гарантированно неуспешные запросы: 401/404/409 и прочее. Только chaos."""
        kind = rng.random()
        if kind < 0.25:
            self.expect_status("login.bad", "POST", "/auth/login", None, 401,
                               {"email": random_email(rng, self.session_id), "password": "wrong"})
        elif kind < 0.45:
            self.expect_status("links.bad_url", "POST", "/api/links", token, 400,
                               {"link": "not-a-url", "tags": []})
        elif kind < 0.65:
            self.expect_status("links.remove_missing", "DELETE", "/api/links", token, 404,
                               {"link": random_link(rng)})
        elif kind < 0.85:
            self.expect_status("links.add_dup", "POST", "/api/links", token, 409,
                               {"link": rng.choice(self.links_to_track), "tags": []})
        else:
            self.request("bad_token", "GET", "/api/links", "invalid-token")

    # --- сводка

    def percentile(self, xs: list[float], p: float) -> float:
        if not xs:
            return 0.0
        ys = sorted(xs)
        k = max(0, min(len(ys) - 1, int(round((p / 100) * (len(ys) - 1)))))
        return ys[k]

    def report(self, wall_seconds: float) -> None:
        print(f"\n{'эндпоинт':<22} {'rps':>7} {'count':>7} {'p50':>8} {'p95':>8} {'p99':>8} {'err%':>7}  коды")
        print("-" * 100)
        total = 0
        total_err = 0
        for name, s in sorted(self.stats.items()):
            with s.lock:
                n = s.counts
                lat = list(s.latencies)
                statuses = dict(s.statuses)
                err = s.errors
            total += n
            total_err += err
            codes = " ".join(f"{k}x:{v}" for k, v in sorted(statuses.items()))
            rps = n / wall_seconds if wall_seconds > 0 else 0
            print(f"{name:<22} {rps:>7.2f} {n:>7} "
                  f"{self.percentile(lat, 50) * 1000:>7.0f}ms {self.percentile(lat, 95) * 1000:>7.0f}ms "
                  f"{self.percentile(lat, 99) * 1000:>7.0f}ms {100 * err / n if n else 0:>6.1f}%  {codes}")
        print("-" * 100)
        err_ratio = 100 * total_err / total if total else 0.0
        print(f"итого: {total} запросов за {wall_seconds:.0f}с, доля ошибок (>=400): {err_ratio:.1f}%")

    def cleanup_links(self) -> None:
        """Удалить ссылки, добавленные генератором (по тегу loadgen)."""
        print("\n[loadgen] cleanup: удаляю добавленные ссылки...")
        removed = 0
        for sess in self.sessions:
            token = sess["token"]
            for item_url in self.tracked_links(token):
                if item_url.startswith(("https://github.com/", "https://stackoverflow.com/")):
                    self.remove_link(token, item_url)
                    removed += 1
        print(f"[loadgen] cleanup: удалено {removed} ссылок")

    def run(self) -> None:
        threads = []
        for i in range(self.users):
            t = threading.Thread(target=self.worker, args=(i,), daemon=True)
            t.start()
            threads.append(t)
            time.sleep(0.05)  # плавный разгон

        try:
            time.sleep(self.duration)
        except KeyboardInterrupt:
            print("\n[loadgen] прервано пользователем")
        self.stop.set()
        time.sleep(0.5)
        self.report(self.duration)
        if self.cleanup:
            self.cleanup_links()


def main() -> None:
    parser = argparse.ArgumentParser(description="Нагрузка на веб-часть Link Tracker")
    parser.add_argument("--base-url", default="http://localhost:30080",
                        help="базовый URL (frontend NodePort или scrapper напрямую)")
    parser.add_argument("--users", type=int, default=5, help="число параллельных пользователей")
    parser.add_argument("--rps", type=int, default=20, help="суммарный целевой rps")
    parser.add_argument("--duration", type=str, default="5m",
                        help="длительность: 30s, 10m, 1h ...")
    parser.add_argument("--chaos", action="store_true", help="подмешивать неуспешные сценарии")
    parser.add_argument("--chaos-ratio", type=float, default=0.1, help="доля неуспешных сценариев при --chaos")
    parser.add_argument("--link", action="append", metavar="URL",
                        help="ссылка для отслеживания (можно несколько раз; по умолчанию — репозитории проекта)")
    parser.add_argument("--cleanup", action="store_true", help="попробовать удалить нагенерированные ссылки в конце")
    parser.add_argument("--seed", type=int, default=42)
    args = parser.parse_args()

    # преобразование duration
    m = re.fullmatch(r"(\d+)([smh])", args.duration)
    if not m:
        parser.error("--duration: формат 30s / 10m / 1h")
    args.duration = int(m.group(1)) * {"s": 1, "m": 60, "h": 3600}[m.group(2)]

    print(f"[loadgen] base={args.base_url} users={args.users} rps={args.rps} "
          f"duration={args.duration}s chaos={args.chaos} ratio={args.chaos_ratio}")
    LoadGen(args).run()


if __name__ == "__main__":
    main()
