package backend.academy.linktracker.bot.application.bot;

import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommandDispatcher {

    private final Map<CommandType, CommandInterface> handlers;
    private final MessageSenderService messageSenderService;

    @Autowired
    public CommandDispatcher(List<CommandInterface> commandBeans, MessageSenderService messageSenderService) {
        this.messageSenderService = messageSenderService;

        this.handlers =
                commandBeans.stream().collect(Collectors.toMap(CommandInterface::getCommandType, handler -> handler));

        log.info("Загруженные обработчики: {}", handlers.keySet());
    }

    public void dispatch(Long chatId, String text) {
        log.info("Получено сообщение: {} от chatId: {}", text, chatId);

        CommandType type = CommandType.fromText(text);

        CommandInterface handler = handlers.get(type);

        try {
            String response = handler.execute(chatId, text);
            messageSenderService.sendText(chatId, response);
        } catch (Exception e) {
            log.error("Произошла при обработке команды");
        }
    }
}
