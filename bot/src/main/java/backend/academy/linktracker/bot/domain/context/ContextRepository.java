package backend.academy.linktracker.bot.domain.context;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ContextRepository {
    /**
     * Возвращает тип активного контекста для пользователя.
     */
    Optional<ContextType> read(Long chatId);

    /**
     * Устанавливает активный контекст.
     */
    void save(Long chatId, ContextType type);

    /**
     * Удаляет запись о контексте (завершение процесса).
     */
    void delete(Long chatId);
}
