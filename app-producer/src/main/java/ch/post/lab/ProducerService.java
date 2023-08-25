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
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RestController
public class ProducerService implements SchedulingConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerService.class);
    private static final String TOPIC = "app.lab.Bar";
    private final Producer<String, String> producer;
    private long recordsPerSecond = 1;

    public ProducerService(KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
        this.producer = new KafkaProducer<>(kafkaProperties.buildProducerProperties(), new StringSerializer(), new StringSerializer());
        new KafkaClientMetrics(producer).bindTo(meterRegistry);
    }

    @GetMapping("/produce/{recordsPerSecond}")
    public long produce(@PathVariable long recordsPerSecond) {
        this.recordsPerSecond = recordsPerSecond;
        LOGGER.info("Producing {} records/second", recordsPerSecond);
        return this.recordsPerSecond;
    }

    private void produce() {
        if (this.recordsPerSecond > 0) {
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
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(this::produce, triggerContext -> {
                    if (recordsPerSecond == 0) {
                        return Objects.requireNonNullElse(triggerContext.lastActualExecution(), Instant.now())
                                .plusMillis(1000);
                    } else {
                        return Objects.requireNonNullElse(triggerContext.lastActualExecution(), Instant.now())
                                .plusMillis(1000 / recordsPerSecond);
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
