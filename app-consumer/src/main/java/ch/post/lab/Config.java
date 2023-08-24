package ch.post.lab;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class Config {
    public static final String TOPIC_FOO = "app.lab.Foo";

}
