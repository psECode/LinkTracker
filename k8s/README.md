# развертывание в minikube


```bash
# можешь с другим драйвером, мне было интересно через виртуалку (значения кластера выбраны примерно под стек)
minikube start --driver=virtualbox --memory=6000 --cpus=3

docker compose build
minikube image load link-tracker-scrapper link-tracker-bot link-tracker-ai-agent \
                    link-tracker-migrations link-tracker-frontend

# конфиги
kubectl apply -f k8s/00-namespace.yaml
kubectl -n link-tracker create secret generic link-tracker-secrets --from-env-file=.env
kubectl apply -f k8s/01-configmap.yaml -f k8s/02-secret-postgres.yaml

# самоподписанный сертификат для https  (если на докере делаешь то можно и не делать, я сейчас на
# виртуалке поэтому там реальный ip, а не localhost поэтому сайт не считается доверенным
# из-за этого уведомления автоматически баннятся
MINIKUBE_IP=$(minikube ip)
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout /tmp/tls.key -out /tmp/tls.crt -days 825 \
  -subj "/CN=$MINIKUBE_IP" -addext "subjectAltName=IP:$MINIKUBE_IP"
kubectl -n link-tracker create secret tls frontend-tls --cert=/tmp/tls.crt --key=/tmp/tls.key

# инфра
kubectl apply -f k8s/10-postgres.yaml -f k8s/11-migrations-job.yaml \
              -f k8s/12-redis.yaml -f k8s/13-kafka.yaml -f k8s/14-schema-registry.yaml

# миграция -> приложения
kubectl -n link-tracker wait --for=condition=complete job/db-migrations --timeout=300s
kubectl apply -f k8s/20-scrapper.yaml -f k8s/21-bot.yaml -f k8s/22-ai-agent.yaml -f k8s/30-frontend.yaml

# логи: Victoria Logs + сборщик Vector (см. раздел "Логирование и Victoria Logs" ниже)
kubectl apply -f k8s/40-victoria-logs.yaml -f k8s/41-vector-logs.yaml

# образы Victoria Logs / Vector можно заранее загрузить в minikube
# (если pull из docker hub внутри кластера не проходит из-за vpn/fake-dns):
docker pull victoriametrics/victoria-logs:v1.53.0
docker pull timberio/vector:0.60.0-debian
minikube image load victoriametrics/victoria-logs:v1.53.0 timberio/vector:0.60.0-debian

# можно проверить
kubectl -n link-tracker get pods,svc
curl http://$(minikube ip):30080/
echo "https://$(minikube ip):30443/"

# метрики 
helm install vmsingle vm/victoria-metrics-single -f k8s/guide-vmsingle-values.yaml
docker pull quay.io/prometheuscommunity/postgres-exporter:v0.20.1
minikube image load victoriametrics/victoria-metrics:v1.136.0 \
                    quay.io/prometheuscommunity/postgres-exporter:v0.20.1
```

# можно проверить
kubectl -n link-tracker get pods,svc
curl http://$(minikube ip):30080/
echo "https://$(minikube ip):30443/"
