#!/usr/bin/env python3
"""Рендер k8s/60-grafana.yaml из канонических дашбордов.

Читает все *.json из "grafana_prometheus things/" (кроме dashboard.json —
дашборд прошлого ДЗ и sli-slo-dashboard.json — старый общий), оборачивает
каждый в ConfigMap grafana-dashboard-<name> и собирает манифест Grafana
(datasources, provider, Deployment, Service).

Запуск:  python3 scripts/render-dashboards.py
"""
from __future__ import annotations

import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "grafana_prometheus things"
DST = ROOT / "k8s" / "60-grafana.yaml"

SKIP = {"dashboard.json", "sli-slo-dashboard.json"}

HEADER = """\
# Grafana + дашборды Link Tracker. Разворачивается в link-tracker.
# UI: http://<minikube-ip>:30500 (admin/admin)
# Дашборды: Обзор (SLI/SLO), Scrapper, Frontend — по k8s/SLI-SLO-SLA.md.
# Перегенерация: python3 scripts/render-dashboards.py
---"""


def indent(text: str, pad: str = "    ") -> str:
    return "\n".join(pad + line if line.strip() else pad for line in text.splitlines())


def dashboard_name(path: pathlib.Path) -> str:
    return path.stem.replace("_", "-")


def main() -> int:
    files = sorted(p for p in SRC.glob("*.json") if p.name not in SKIP)
    if not files:
        print("нет дашбордов для рендера", file=sys.stderr)
        return 1

    out = [HEADER]

    # --- datasources ---
    out.append("""\
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-datasources
  namespace: link-tracker
  labels:
    app: grafana
data:
  datasources.yaml: |
    apiVersion: 1
    datasources:
      - name: VictoriaMetrics
        uid: vmsingle
        type: prometheus
        access: proxy
        url: http://vmsingle-victoria-metrics-single-server.default.svc.cluster.local:8428
        isDefault: true
        jsonData:
          timeInterval: 15s
      - name: VictoriaLogs
        uid: victorialogs
        type: victoriametrics-logs-datasource
        access: proxy
        url: http://victoria-logs.link-tracker.svc.cluster.local:9428
        jsonData:
          maxLinesPerQuery: 1000""")

    # --- provider ---
    out.append("""\
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboards-provider
  namespace: link-tracker
  labels:
    app: grafana
data:
  provider.yaml: |
    apiVersion: 1
    providers:
      - name: default
        folder: Link Tracker
        type: file
        options:
          path: /var/lib/grafana/dashboards
          foldersFromFilesStructure: false""")

    # --- dashboards ---
    for path in files:
        try:
            doc = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            print(f"{path.name}: битый JSON: {e}", file=sys.stderr)
            return 1
        name = dashboard_name(path)
        body = json.dumps(doc, ensure_ascii=False, indent=2)
        out.append(
            f"""\
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboard-{name}
  namespace: link-tracker
  labels:
    app: grafana
data:
  {path.name}: |
{indent(body)}"""
        )

    # --- deployment + service ---
    out.append("""\
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: link-tracker
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
        - name: grafana
          image: grafana/grafana:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 3000
              name: http
          env:
            - name: GF_SECURITY_ADMIN_USER
              value: admin
            - name: GF_SECURITY_ADMIN_PASSWORD
              value: admin
            - name: GF_AUTH_ANONYMOUS_ENABLED
              value: "true"
            - name: GF_AUTH_ANONYMOUS_ORG_ROLE
              value: Viewer
            - name: GF_INSTALL_PLUGINS
              value: victoriametrics-logs-datasource
          volumeMounts:
            - name: datasources
              mountPath: /etc/grafana/provisioning/datasources
            - name: dashboards-provider
              mountPath: /etc/grafana/provisioning/dashboards
            - name: dashboards
              mountPath: /var/lib/grafana/dashboards
          readinessProbe:
            httpGet:
              path: /api/health
              port: 3000
            periodSeconds: 10
          resources:
            requests:
              cpu: 100m
              memory: 256Mi
            limits:
              memory: 512Mi
      volumes:
        - name: datasources
          configMap:
            name: grafana-datasources
        - name: dashboards-provider
          configMap:
            name: grafana-dashboards-provider
        - name: dashboards
          projected:
            sources:
""" + "\n".join(
                f"""\
              - configMap:
                  name: grafana-dashboard-{dashboard_name(f)}"""
                for f in files
            ))

    out.append("""\
---
apiVersion: v1
kind: Service
metadata:
  name: grafana
  namespace: link-tracker
spec:
  type: NodePort
  selector:
    app: grafana
  ports:
    - port: 3000
      targetPort: 3000
      nodePort: 30500""")

    DST.write_text("\n".join(out) + "\n", encoding="utf-8")
    print(f"ok: {DST} ({len(files)} дашбордов: {', '.join(p.name for p in files)})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
