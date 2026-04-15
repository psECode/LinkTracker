package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.infrastructure.api.dtos.ApiErrorResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BotExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return new ApiErrorResponse(
                "Некорректные параметры запроса",
                "400",
                e.getClass().getSimpleName(),
                errorMessage,
                Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
    }
}
