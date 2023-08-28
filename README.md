# Kafka-clients

## Installation
```
kubectl apply -f k8s/kafka/namespace.yml
kubectl -n kafka apply -f 'https://strimzi.io/install/latest?namespace=kafka' 
kubectl -n kafka apply -f k8s/kafka/kafka.yml
kubectl -n kafka apply -f k8s/kafka/kafka-lag-exporter.yml
```
```
kubectl apply -f k8s/monitoring/namespace.yml
kubectl -n monitoring apply -f k8s/monitoring/prometheus.yml
kubectl -n monitoring create configmap grafana-dashboards-conf --from-file=k8s/monitoring/grafana/dashboards 
kubectl -n monitoring apply -f k8s/monitoring/grafana.yml
```
```
kubectl apply --server-side -f https://github.com/kedacore/keda/releases/download/v2.11.2/keda-2.11.2.yaml
```
```
export JAVA_HOME=$(/usr/libexec/java_home)
cd app-producer
mvn clean install spring-boot:build-image
cd ..
cd app-processor
mvn clean install spring-boot:build-image
cd ..
cd app-consumer
mvn clean install spring-boot:build-image
cd ..
kubectl apply -f k8s/app/namespace.yml
kubectl -n kafka apply -f k8s/app/app-topics.yml
kubectl -n app apply -f k8s/app/app-producer.yml
kubectl -n app apply -f k8s/app/app-consumer.yml
kubectl -n app apply -f k8s/app/app-consumer-scaler.yml
kubectl -n app apply -f k8s/app/app-processor.yml
kubectl -n app apply -f k8s/app/app-processor-scaler.yml
```
## Commands
```
kubectl -n app get pods 
kubectl -n app get all 
kubectl -n kafka get all 
kubectl -n monitoring get pods 
kubectl -n app logs -f pod/app-consumer-c78c5587c-svt98
kubectl -n app logs -f deployment/app-consumer --all-containers=true
kubectl -n app logs -f deployment/app-producer --all-containers=true
kubectl -n app logs -f deployment/app-processor --all-containers=true
kubectl -n monitoring get events --sort-by='.metadata.creationTimestamp'
kubectl -n monitoring get serviceaccounts/prometheus -o yaml
```
## Uninstall
```
kubectl delete namespace app
kubectl delete namespace monitoring
kubectl delete namespace kafka
kubectl delete -f https://github.com/kedacore/keda/releases/download/v2.11.2/keda-2.11.2.yaml
kubectl delete -f 'https://strimzi.io/install/latest?namespace=kafka' 
```

### app-producer
http://localhost:8070/actuator/prometheus
curl http://localhost:8070/produce/10

### app-processor
http://localhost:8080/actuator/prometheus
curl http://localhost:8080/processing/1

### app-consumer
http://localhost:8090/actuator/prometheus
curl http://localhost:8090/consume/100
curl http://localhost:8090/processing/1000
curl http://localhost:8090/consumer-poll/10

### Infrastructure
[Kafka-lag-exporter](http://localhost:9999)
[Prometheus](http://localhost:9090)
[Grafana](http://localhost:3000)
Bootstrap server: localhost:32000

# Kafka client metrics
The window of time a metrics sample is computed over: metrics.sample.window.ms (30 seconds) 

# TODO
- Dashboards
  - kafka streams: complete
  - k8s: https://github.com/grafana/kubernetes-app/tree/master/src/dashboards
         https://github.com/dotdc/grafana-dashboards-kubernetes
  - Brokers
    - https://github.com/strimzi/strimzi-kafka-operator/tree/main/examples/metrics/grafana-dashboards

