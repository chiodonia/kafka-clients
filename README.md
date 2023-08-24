# Monitoring kafka-clients

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
kubectl -n app apply -f k8s/app/app-processor.yml
kubectl -n app apply -f k8s/app/app-consumer.yml
kubectl -n app apply -f k8s/app/app-producer.yml
kubectl -n app apply -f k8s/app/app-processor-autoscaling.yml
kubectl -n app apply -f k8s/app/app-consumer-autoscaling.yml
```
## Commands
```
kubectl -n app get pods 
kubectl -n app get all 
kubectl -n kafka get pods 
kubectl -n monitoring get pods 
kubectl -n monitoring logs -f prometheus-c6d444977-8rkgs
kubectl -n app logs -f deployment/app-consumer --all-containers=true
kubectl -n app get events --sort-by='.metadata.creationTimestamp'
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
http://localhost:7070/actuator/prometheus
curl http://localhost:7070/produce/1

### app-processor
http://localhost:9090/actuator/prometheus
curl http://localhost:8080/processing/10

### app-consumer
http://localhost:9090/actuator/prometheus
curl http://localhost:9090/consume/1
curl http://localhost:9090/processing/10
curl http://localhost:9090/consumer-poll/10

### Infrastructure
[Kafka-lag-exporter](http://localhost:9999)
[Prometheus](http://localhost:9090)
[Grafana](http://localhost:3000)
Bootstrap server: localhost:32000

# Metrics
The window of time a metrics sample is computed over: metrics.sample.window.ms (30 seconds) 
## Consumer

### TYPE kafka_consumer_fetch_manager_records_lag gauge
kafka_consumer_fetch_manager_records_lag{client_id="app.consumer",kafka_version="3.5.1",partition="4",topic="app_lab_Foo",} 0.0

### TYPE kafka_consumer_fetch_manager_records_lag_max gauge
kafka_consumer_fetch_manager_records_lag_max{client_id="app.consumer",kafka_version="3.5.1",partition="4",topic="app_lab_Foo",} 9.0

### TYPE kafka_consumer_fetch_manager_records_lag_avg gauge
kafka_consumer_fetch_manager_records_lag_avg{client_id="app.consumer",kafka_version="3.5.1",partition="4",topic="app_lab_Foo",} 4.090909090909091

# TODO
- Dashboards
  - k8s: https://github.com/grafana/kubernetes-app/tree/master/src/dashboards
         https://github.com/dotdc/grafana-dashboards-kubernetes
  - JVM:
  - Kafka consumer
  - kafka producer
  - kafka streams
  - Brokers

