package ch.post.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static final String TOPIC = "test-topic";
    public static final long INITIAL_DELAY = 1000L;
    public static final long PRODUCER_DELAY = 1000L;
    public static final long CONSUMER_DELAY = 10L;
    public static final long CONSUMER_POLL = 10L;
    public static final long CONSUMER_RECORD_PROCESSING = 2000L;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
