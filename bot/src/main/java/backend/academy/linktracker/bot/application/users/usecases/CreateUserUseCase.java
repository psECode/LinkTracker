package backend.academy.linktracker.bot.application.users.usecases;

import backend.academy.linktracker.bot.domain.users.UsersRepository;
import backend.academy.linktracker.bot.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.bot.domain.users.entities.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateUserUseCase {
    private final UsersRepository usersRepository;

    public Optional<User> execute(CreateUserDto dto) {
        return usersRepository.save(dto);
    }
}
