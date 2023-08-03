package ch.post.lab;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static ch.post.lab.Application.*;

@Service
public class ProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerService.class);
    private final Producer<String, String> producer;

    public ProducerService(KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
        this.producer = new KafkaProducer<>(kafkaProperties.buildProducerProperties(), new StringSerializer(), new StringSerializer());
        new KafkaClientMetrics(producer).bindTo(meterRegistry);
    }

    @Scheduled(initialDelay = INITIAL_DELAY, fixedRate = PRODUCER_DELAY)
    public void produce() {
        producer.send(
                new ProducerRecord<>(TOPIC, key(), value()),
                (metadata, e) -> {
                    if (e == null) {
                        LOGGER.info("Produced: {}", metadata);
                    } else {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private String key() {
        return UUID.randomUUID().toString();
    }

    private String value() {
        return UUID.randomUUID().toString();
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
