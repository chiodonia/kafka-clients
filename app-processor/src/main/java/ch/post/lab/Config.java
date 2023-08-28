package ch.post.lab;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener;

@Configuration
@EnableKafkaStreams
public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

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

}
