package backend.academy.linktracker.bot.application.bot;

import backend.academy.linktracker.bot.application.context.usecases.DeleteActiveContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.ReadActiveContextUseCase;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import backend.academy.linktracker.bot.domain.context.ContextHandler;
import backend.academy.linktracker.bot.domain.context.ContextHandlerFactory;
import backend.academy.linktracker.bot.domain.context.ContextResult;
import backend.academy.linktracker.bot.domain.context.ContextType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommandDispatcher {

    private final Map<CommandType, CommandInterface> commandHandlers;
    private final ContextHandlerFactory flowHandlerFactory;
    private final ReadActiveContextUseCase readActiveContext;
    private final DeleteActiveContextUseCase deleteActiveContext;
    private final MessageSenderService messageSender;

    public CommandDispatcher(
            List<CommandInterface> commandBeans,
            ContextHandlerFactory flowHandlerFactory,
            ReadActiveContextUseCase readActiveContext,
            DeleteActiveContextUseCase deleteActiveContext,
            MessageSenderService messageSender) {
        this.flowHandlerFactory = flowHandlerFactory;
        this.readActiveContext = readActiveContext;
        this.deleteActiveContext = deleteActiveContext;
        this.messageSender = messageSender;
        this.commandHandlers =
                commandBeans.stream().collect(Collectors.toMap(CommandInterface::getCommandType, c -> c));
    }

    public void dispatch(Long chatId, String text) {
        log.info("Message from {}: {}", chatId, text);

        try {
            ContextType activeContext = readActiveContext.execute(chatId).orElseThrow();

            ContextHandler handler =
                    flowHandlerFactory.getHandler(activeContext).orElseThrow();

            ContextResult result = handler.handle(chatId, text);

            if (result.handled()) {
                messageSender.sendText(chatId, result.message());
                if (result.isFinished()) {
                    deleteActiveContext.execute(chatId);
                }
                return;
            } else {
                deleteActiveContext.execute(chatId);
            }

            processAsCommand(chatId, text);
        } catch (Exception e) {
            processAsCommand(chatId, text);
        }
    }

    private void processAsCommand(Long chatId, String text) {
        CommandType type = CommandType.fromText(text);
        CommandInterface handler = commandHandlers.getOrDefault(type, commandHandlers.get(CommandType.UNKNOWN));
        messageSender.sendText(chatId, handler.execute(chatId, text));
    }
}
