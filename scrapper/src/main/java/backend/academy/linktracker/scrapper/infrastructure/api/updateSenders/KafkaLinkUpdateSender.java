package backend.academy.linktracker.scrapper.infrastructure.api.updateSenders;

import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxRepository;
import backend.academy.linktracker.scrapper.domain.outboxMessages.dtos.CreateOutboxMessageDTO;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class KafkaLinkUpdateSender implements LinkUpdateSender {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    @Transactional
    public void send(LinkUpdate update) {
        String json = objectMapper.writeValueAsString(update);
        outboxRepository.save(new CreateOutboxMessageDTO(json));
    }
}
