package backend.academy.linktracker.scrapper.infrastructure.api.usecases;

import backend.academy.linktracker.scrapper.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateUserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserUseCase {
    private final CreateUserUseCase createUserUseCase;

    @Transactional
    public void execute(Long chatId) {
        CreateUserDto dto = new CreateUserDto(chatId);
        createUserUseCase.execute(dto);
    }
}
