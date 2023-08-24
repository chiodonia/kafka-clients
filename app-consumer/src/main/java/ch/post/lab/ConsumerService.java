package ch.post.lab;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static ch.post.lab.Config.TOPIC;

@RestController
public class ConsumerService implements SchedulingConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerService.class);
    private final Consumer<String, String> consumer;
    private long recordsPerSecond = 1;
    private Duration processingDuration = Duration.ofMillis(0L);
    private Duration consumerPollDuration = Duration.ofMillis(10L);

    public ConsumerService(KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
        this.consumer = new KafkaConsumer<>(
                kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new StringDeserializer());
        this.consumer.subscribe(Collections.singleton(TOPIC), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                partitions.forEach(partition -> LOGGER.debug("Partition revoked {}", partition));
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                partitions.forEach(partition -> LOGGER.debug("Partition assigned {}", partition));
            }
        });
        new KafkaClientMetrics(consumer).bindTo(meterRegistry);
    }

    @GetMapping("/consume/{recordsPerSecond}")
    long produce(@PathVariable long recordsPerSecond) {
        this.recordsPerSecond = recordsPerSecond;
        LOGGER.info("Consuming {} records/second", recordsPerSecond);
        return this.recordsPerSecond;
    }

    @GetMapping("/processing/{durationMillis}")
    long processing(@PathVariable long durationMillis) {
        this.processingDuration = Duration.ofMillis(durationMillis);
        LOGGER.info("Processing will take {} Millis", processingDuration.toMillis());
        return processingDuration.toMillis();
    }

    @GetMapping("/consumer-poll/{durationMillis}")
    long consumerPoll(@PathVariable long durationMillis) {
        this.consumerPollDuration = Duration.ofMillis(durationMillis);
        LOGGER.info("Consumer will poll for {} Millis", consumerPollDuration.toMillis());
        return consumerPollDuration.toMillis();
    }

    private void consume() {
        try {
            ConsumerRecords<String, String> records = consumer.poll(consumerPollDuration);
            LOGGER.debug("Consumed {} records", records.count());
            records.forEach(this::process);
            consumer.commitSync();
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }
    }

    private void process(ConsumerRecord<String, String> record) {
        try {
            Thread.sleep(processingDuration.toMillis());
            LOGGER.info("Processed {}-{}@{}", record.topic(), record.partition(), record.offset());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(this::consume, triggerContext -> {
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

    @PreDestroy
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }
}