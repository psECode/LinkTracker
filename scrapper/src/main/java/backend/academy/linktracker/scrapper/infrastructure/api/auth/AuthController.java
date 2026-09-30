package backend.academy.linktracker.scrapper.infrastructure.api.auth;

import backend.academy.linktracker.scrapper.application.users.usecases.AuthenticateWebUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.application.users.usecases.RegisterWebUserUseCase;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.dtos.LoginRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.dtos.MeResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.dtos.RegisterRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.dtos.TokenResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidCredentialsException;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterWebUserUseCase registerWebUserUseCase;
    private final AuthenticateWebUserUseCase authenticateWebUserUseCase;
    private final ReadUserService readUserService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/register")
    public ResponseEntity<MeResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerWebUserUseCase.execute(request.email(), request.password());
        log.info("Зарегистрирован веб-пользователь {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toMe(user));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authenticateWebUserUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(
                new TokenResponse(jwtTokenService.issue(user.getId()), "Bearer", jwtTokenService.ttlSeconds()));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = readUserService
                .readByUUID(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Пользователь не найден"));
        return ResponseEntity.ok(toMe(user));
    }

    private MeResponse toMe(User user) {
        return new MeResponse(user.getId(), user.getEmail(), user.getTelegramId() != null);
    }
}
