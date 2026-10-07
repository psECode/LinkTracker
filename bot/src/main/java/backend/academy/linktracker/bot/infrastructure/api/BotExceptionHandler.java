package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.infrastructure.api.dtos.ApiErrorResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Стектрейсы не отдаются в ответах наружу — они остаются в логах приложения.
@Slf4j
@RestControllerAdvice
public class BotExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Обработана ошибка 400: некорректные параметры запроса");

        return new ApiErrorResponse(
                "Некорректные параметры запроса", "400", e.getClass().getSimpleName(), errorMessage);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiErrorResponse handleRateLimitException(RequestNotPermitted e) {
        log.warn("Обработана ошибка 429: превышен лимит запросов");

        return new ApiErrorResponse(
                "Превышен лимит запросов. Попробуйте позже.", "429", "RateLimitExceededException", e.getMessage());
    }
}
