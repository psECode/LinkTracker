package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.domain.api.dtos.ApiErrorResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidLinkException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.SubscriptionNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScrapperExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, LinkNotFoundException.class, SubscriptionNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "Запрашиваемый ресурс не найден");
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex) {
        return buildResponse(ex, HttpStatus.CONFLICT, "Ресурс уже существует");
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidLinkException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "Некорректные параметры запроса");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleInternalError(Exception ex) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(Exception ex, HttpStatus status, String description) {
        List<String> stacktrace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        ApiErrorResponse errorDTO = new ApiErrorResponse(
                description,
                String.valueOf(status.value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                stacktrace);

        return new ResponseEntity<>(errorDTO, status);
    }
}
