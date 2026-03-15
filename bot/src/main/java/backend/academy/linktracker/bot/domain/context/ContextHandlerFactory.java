package backend.academy.linktracker.bot.domain.context;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContextHandlerFactory {

    private final Map<ContextType, ContextHandler> handlers;

    @Autowired
    public ContextHandlerFactory(List<ContextHandler> handlerBeans) {
        // Собираем мапу: Тип из Enum -> Бин обработчика
        this.handlers = handlerBeans.stream().collect(Collectors.toMap(ContextHandler::getSupportedType, h -> h));
    }

    public Optional<ContextHandler> getHandler(ContextType type) {
        return Optional.ofNullable(handlers.get(type));
    }
}
