package backend.academy.linktracker.scrapper.infrastructure.api.updateSenders;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.AppProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ScrapperUpdateSender implements LinkUpdateSender {

    private final LinkUpdateSender httpSender;
    private final LinkUpdateSender kafkaSender;
    private final AppProperties properties;

    @Override
    @SneakyThrows
    @CircuitBreaker(name = "botUpdateCB", fallbackMethod = "useKafkaFallback")
    public void send(LinkUpdate update) {
        if (properties.isUseQueue()) {
            kafkaSender.send(update);
        } else {
            httpSender.send(update);
        }
    }

    @SneakyThrows
    public void useKafkaFallback(LinkUpdate update, Throwable t) {
        log.error("Falling back to Kafka. Reason: {}", t.getMessage());
        kafkaSender.send(update);
    }
}
