package ch.post.lab;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@EnableKafkaStreams
public class ProcessorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorConfig.class);
    private static final String TOPIC_BAR = "app.lab.Bar";
    private static final String TOPIC_FOO = "app.lab.Foo";
    public static final String PREFIX = "foo.";
    private Duration processingDuration = Duration.ofMillis(0L);
    @Autowired
    private MeterRegistry meterRegistry;

    @Bean
    public StreamsBuilderFactoryBeanConfigurer configurer() {
        return fb -> {
            fb.addListener(new KafkaStreamsMicrometerListener(meterRegistry));
            fb.setStateListener((newState, oldState) -> {
                LOGGER.debug("State transition from {} to {}", oldState, newState);
            });
        };
    }

    @GetMapping("/processing/{durationMillis}")
    long processing(@PathVariable long durationMillis) {
        this.processingDuration = Duration.ofMillis(durationMillis);
        LOGGER.info("Processing will take {} Millis", processingDuration.toMillis());
        return processingDuration.toMillis();
    }

    @Bean
    public KStream<String, String> processor(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(TOPIC_BAR, Consumed.with(
                                Serdes.String(),
                                Serdes.String()
                        )
                )
                .peek((key, value) -> {
                    try {
                        Thread.sleep(processingDuration.toMillis());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .mapValues((key, value) -> PREFIX.concat(value))
                .processValues(() -> new ContextualFixedKeyProcessor<>() {
                    @Override
                    public void process(FixedKeyRecord<String, String> record) {
                        context().recordMetadata().ifPresent(metadata -> LOGGER.info("Processed {}-{}@{}", metadata.topic(), metadata.partition(), metadata.offset()));
                    }
                });

        stream.to(TOPIC_FOO, Produced.with(Serdes.String(), Serdes.String()));

        return stream;
    }

}
