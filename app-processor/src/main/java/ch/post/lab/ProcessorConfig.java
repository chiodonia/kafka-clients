package ch.post.lab;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class ProcessorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorConfig.class);
    public static final String TOPIC_BAR = "app.lab.Bar";
    public static final String TOPIC_FOO = "app.lab.Foo";
    public static final String PREFIX = "foo.";
    private Duration processingDuration = Duration.ofMillis(0L);

    public ProcessorConfig(StreamsBuilder builder) {
        topology(builder);
    }

    private void topology(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(TOPIC_BAR, Consumed.with(Serdes.String(), Serdes.String()))
                .peek((key, value) -> {
                    try {
                        Thread.sleep(processingDuration.toMillis());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .mapValues((key, value) -> PREFIX.concat(value));
        stream.print(Printed.toSysOut());
        stream.to(TOPIC_FOO, Produced.with(Serdes.String(), Serdes.String()));
    }

    @GetMapping("/processing/{durationMillis}")
    public long processing(@PathVariable long durationMillis) {
        this.processingDuration = Duration.ofMillis(durationMillis);
        LOGGER.info("Processing will take {} Millis", processingDuration.toMillis());
        return processingDuration.toMillis();
    }

}
