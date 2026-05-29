package backend.academy.linktracker.ai.processors;

import com.example.notification.ProcessedUpdateEvent;
import com.example.notification.RawUpdateEvent;
import java.util.Optional;

public interface MessageProcessor {
    Optional<ProcessedUpdateEvent> process(RawUpdateEvent raw);
}
