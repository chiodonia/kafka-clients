package ch.post.lab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/kstream")
public class KStreamApi {

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @GetMapping("/topology")
    public String topology() {
        return Objects.requireNonNull(streamsBuilderFactoryBean.getTopology()).describe().toString();
    }
}
