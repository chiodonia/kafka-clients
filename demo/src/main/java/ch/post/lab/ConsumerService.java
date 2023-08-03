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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

import static ch.post.lab.Application.*;

@Service
public class ConsumerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerService.class);

    private final Consumer<String, String> consumer;

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

    @Scheduled(initialDelay = INITIAL_DELAY, fixedRate = CONSUMER_DELAY)
    public void consume() {
        try {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(CONSUMER_POLL));
            LOGGER.debug("Consumed {} records", records.count());
            records.forEach(this::process);
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }
    }

    private void process(ConsumerRecord<String, String> record) {
        try {
            Thread.sleep(CONSUMER_RECORD_PROCESSING);
            LOGGER.info("Consumed {}-{}@{}", record.topic(), record.partition(), record.offset());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }
}