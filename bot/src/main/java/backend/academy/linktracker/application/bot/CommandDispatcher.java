package backend.academy.linktracker.application.bot;

import backend.academy.linktracker.domain.bot.CommandInterface;
import backend.academy.linktracker.domain.bot.CommandType;
import backend.academy.linktracker.domain.bot.MessageSenderPort;
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
    private final MessageSenderPort messageSenderPort;

    @Autowired
    public CommandDispatcher(List<CommandInterface> commandBeans, MessageSenderPort messageSenderPort) {
        this.messageSenderPort = messageSenderPort;

        this.handlers =
                commandBeans.stream().collect(Collectors.toMap(CommandInterface::getCommandType, handler -> handler));

        log.info("Загруженные обработчики: {}", handlers.keySet());
    }

    public void dispatch(Long chatId, String text) {
        log.info("Получено сообщение: {} от chatId: {}", text, chatId);

        CommandType type = CommandType.fromText(text);

        CommandInterface handler = handlers.get(type);

        if (handler == null) {
            handler = handlers.get(CommandType.UNKNOWN);
        }

        if (handler != null) {
            String response = handler.execute(chatId, text);
            messageSenderPort.sendText(chatId, response);
        } else {
            log.error("я вообще не понимаю что сейчас произошло");
        }
    }
}
