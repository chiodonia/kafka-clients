package ch.post.lab;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static ch.post.lab.ProcessorConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessorConfigTests {
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @BeforeEach
    public void beforeEach() {
        final StreamsBuilder builder = new StreamsBuilder();

        new ProcessorConfig(builder);

        testDriver = new TopologyTestDriver(builder.build());

        inputTopic = testDriver.createInputTopic(TOPIC_BAR, Serdes.String().serializer(), Serdes.String().serializer());
        outputTopic = testDriver.createOutputTopic(TOPIC_FOO, Serdes.String().deserializer(), Serdes.String().deserializer());
    }

    @Test
    public void testInputIsProcessed() {
        String key = UUID();
        String value = UUID();
        inputTopic.pipeInput(key, value);
        assertEquals(outputTopic.readKeyValue(), new KeyValue<>(key, PREFIX.concat(value)));
        assertTrue(outputTopic.isEmpty());
    }

    @AfterEach
    public void afterEach() {
        testDriver.close();
    }

    private static String UUID() {
        return UUID.randomUUID().toString();
    }
}
