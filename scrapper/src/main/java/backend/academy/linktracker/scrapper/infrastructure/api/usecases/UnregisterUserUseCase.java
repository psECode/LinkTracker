package backend.academy.linktracker.scrapper.infrastructure.api.usecases;

import backend.academy.linktracker.scrapper.application.users.usecases.DeleteUserUseCase;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnregisterUserUseCase {
    private final DeleteUserUseCase deleteUserUseCase;

    @Transactional
    public void execute(UUID userId) {
        deleteUserUseCase.execute(userId);
    }
}
