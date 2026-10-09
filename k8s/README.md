# развертывание в minikube


```bash
# можешь с другим драйвером, мне было интересно через виртуалку (значения кластера выбраны примерно под стек)
minikube start --driver=virtualbox --memory=6000 --cpus=3

# DNS-фикс под vpn/fake-dns хоста (после каждого minikube start)
./k8s/fix-dns.sh

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

# можно проверить
kubectl -n link-tracker get pods,svc
curl http://$(minikube ip):30080/
echo "https://$(minikube ip):30443/"

kubectl -n link-tracker scale deployment scrapper --replicas=3
kubectl -n link-tracker get pods -l app=scrapper -w
kubectl -n link-tracker get deployment scrapper
kubectl -n link-tracker get endpoints scrapper
```

## Окружение с vpn/fake-dns

у меня впн с fake-dns поэтому он не пропускает траффик (поэтому в докере там dns 1.1.1.1 8.8.8.8):
если с впн не работает то можешь попробовать

Готовый скрипт (применять после каждого `minikube start`; эквивалент ручной
правки forward-блока в CoreDNS, внутренние имена *.svc.cluster.local не трогает):

```bash
./k8s/fix-dns.sh            # применить фикс
./k8s/fix-dns.sh --check    # только проверить
./k8s/fix-dns.sh --revert   # вернуть исходный forward на нодный резолвер
```
