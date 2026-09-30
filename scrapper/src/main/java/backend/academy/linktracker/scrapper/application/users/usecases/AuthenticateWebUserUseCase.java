package backend.academy.linktracker.scrapper.application.users.usecases;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticateWebUserUseCase {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public User execute(String email, String rawPassword) {
        User user = usersRepository.readByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("Пользователь деактивирован");
        }

        return user;
    }
}
