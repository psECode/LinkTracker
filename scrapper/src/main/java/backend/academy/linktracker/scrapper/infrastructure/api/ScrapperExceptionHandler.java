package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.logging.LogArgs;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.ApiErrorResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidCredentialsException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidLinkException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.SubscriptionNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserAlreadyExistsException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ScrapperExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, LinkNotFoundException.class, SubscriptionNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, "Запрашиваемый ресурс не найден", true);
   }

    @ExceptionHandler({LinkAlreadyTrackedException.class, UserAlreadyExistsException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex) {
        return buildResponse(ex, HttpStatus.CONFLICT, "Ресурс уже существует", true);
   }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildResponse(ex, HttpStatus.UNAUTHORIZED, "Ошибка аутентификации", true);
   }

    @ExceptionHandler({IllegalArgumentException.class, InvalidLinkException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "Некорректные параметры запроса", true);
   }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "Некорректные параметры запроса", true);
   }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, "Некорректное тело запроса", false);
   }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleInternalError(Exception ex) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", false);
   }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            Exception ex, HttpStatus status, String description, boolean includeDetails) {
        if (status.is5xxServerError()) {
            log.error("Handled API error", LogArgs.of("status", status.value(), "description", description), ex);
       } else {
            log.warn(
                    "Handled API error",
                   LogArgs.of("status", status.value(), "description", description, "exception", ex.getClass().getSimpleName()));
        }

        ApiErrorResponse errorDTO = new ApiErrorResponse(
                description,
                String.valueOf(status.value()),
                includeDetails ? ex.getClass().getSimpleName() : null,
                includeDetails ? ex.getMessage() : null);

        return new ResponseEntity<>(errorDTO, status);
    }
}
