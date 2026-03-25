package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.DeleteUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserByTgIdUseCase;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ReadUserByTgIdUseCase readUserUseCase;

    @Transactional
    public void register(Long chatId) {
        CreateUserDto dto = new CreateUserDto(chatId);
        createUserUseCase.execute(dto);
    }

    @Transactional
    public void unregister(Long chatId) {
        User user = readUserUseCase.execute(chatId).orElseThrow(() -> new UserNotFoundException(chatId));
        deleteUserUseCase.execute(user.getId());
    }
}
