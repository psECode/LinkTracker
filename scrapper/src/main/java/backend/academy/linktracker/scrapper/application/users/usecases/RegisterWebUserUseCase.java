package backend.academy.linktracker.scrapper.application.users.usecases;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateWebUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterWebUserUseCase {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public User execute(String email, String rawPassword) {
        usersRepository.readByEmail(email).ifPresent(user -> {
            throw new UserAlreadyExistsException(email);
        });

        return usersRepository
                .saveWebUser(new CreateWebUserDto(email, passwordEncoder.encode(rawPassword)))
                .orElseThrow(() -> new UserAlreadyExistsException(email));
    }
}
