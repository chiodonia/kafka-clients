# Lag

## [Kubernetes](https://kubernetes.io/)
```
mvn spring-boot:build-image
kubectl create namespace strm-lag
kubectl apply -f strm-lag.yml -n strm-lag
```

```
kubectl get pods,deploy -n strm-lag
kubectl -n strm-lag logs -f pod/cal-5cfb86d9f5-htfss
```

```
kubectl delete namespace strm-lag
```

# Literature
http://localhost:8080/actuator/prometheus
## K8S
https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/#scaling-on-custom-metrics

https://medium.com/@roman.noze/kubernetes-pods-autoscaling-with-kafka-metrics-9b7d5ec3c1d3

## KEDA
https://keda.sh/docs/2.11/deploy/#install-2
https://keda.sh/docs/2.11/scalers/apache-kafka/
https://medium.com/google-cloud/auto-scaling-kafka-consumers-with-kubernetes-and-keda-eb6b6ddb4f34