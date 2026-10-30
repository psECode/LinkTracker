package backend.academy.linktracker.bot.application.bot;

import backend.academy.linktracker.logging.LogArgs;
import backend.academy.linktracker.bot.application.context.usecases.DeleteActiveContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.ReadActiveContextUseCase;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import backend.academy.linktracker.bot.domain.context.ContextHandler;
import backend.academy.linktracker.bot.domain.context.ContextHandlerFactory;
import backend.academy.linktracker.bot.domain.context.ContextResult;
import backend.academy.linktracker.bot.domain.context.ContextType;
import backend.academy.linktracker.logging.MdcTraceId;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    public CommandDispatcher(
            List<CommandInterface> commandBeans,
            ContextHandlerFactory flowHandlerFactory,
            ReadActiveContextUseCase readActiveContext,
            DeleteActiveContextUseCase deleteActiveContext,
            MessageSenderService messageSender,
            MeterRegistry meterRegistry) {
        this.flowHandlerFactory = flowHandlerFactory;
        this.readActiveContext = readActiveContext;
        this.deleteActiveContext = deleteActiveContext;
        this.messageSender = messageSender;
        this.commandHandlers =
                commandBeans.stream().collect(Collectors.toMap(CommandInterface::getCommandType, c -> c));
        this.meterRegistry = meterRegistry;
    }

    public void dispatch(Long chatId, String text) {
        MdcTraceId.run("cmd-" + MdcTraceId.newTraceId(), () -> {
           log.info("Message received", LogArgs.of("chat_id", chatId, "text", text));
           try {
                if (handleActiveContext(chatId, text)) {
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to handle message", LogArgs.of("chat_id", chatId, "error", e.getMessage()), e);
            }
            processAsCommand(chatId, text);
        });
    }

    private boolean handleActiveContext(Long chatId, String text) {
        ContextType activeContext = readActiveContext.execute(chatId).orElse(null);
        if (activeContext == null) {
            return false;
        }
        ContextHandler handler = flowHandlerFactory.getHandler(activeContext).orElse(null);
        if (handler == null) {
            deleteActiveContext.execute(chatId);
            return false;
        }
        ContextResult result = handler.handle(chatId, text);
        if (result.handled()) {
            messageSender.sendText(chatId, result.message());
            if (result.isFinished()) {
                deleteActiveContext.execute(chatId);
            }
            return true;
        }
        deleteActiveContext.execute(chatId);
        return false;
    }

    private void processAsCommand(Long chatId, String text) {
        CommandType type = CommandType.fromText(text);
        CommandInterface handler = commandHandlers.getOrDefault(type, commandHandlers.get(CommandType.UNKNOWN));
        var timer = meterRegistry.timer(
                "command_duration_ms_total", "scope", "scrapper_sync_api", "scope_type", type.name());
       String responseMessage;
        try {
            responseMessage = timer.record(() -> handler.execute(chatId, text));
        } catch (RuntimeException e) {
            meterRegistry.counter("command_requests_total", "command", type.name(), "outcome", "failure").increment();
           throw e;
        }
        meterRegistry.counter("command_requests_total", "command", type.name(), "outcome", "success").increment();
        messageSender.sendText(chatId, responseMessage);
    }
}
