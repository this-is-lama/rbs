package my.project.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.dto.ApiError;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest request) {
        HttpStatus status = ex.getCode().status();
        var locale = LocaleContextHolder.getLocale();

        if (status.is5xxServerError()) {
            log.error("ApiException: code={}, path={}, message={}", ex.getCode(), request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.warn("ApiException: code={}, path={}, message={}", ex.getCode(), request.getRequestURI(), ex.getMessage());
        }

        ApiError body = new ApiError(
                status.value(),
                ex.getCode().code(),
                messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var locale = LocaleContextHolder.getLocale();
        String msgKey = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("common.validation-error");

        String msg = messageSource.getMessage(msgKey, null, msgKey, locale);

        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                msg,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("common.validation-error");

        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                msg,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleBadJson(HttpServletRequest request) {
        var locale = LocaleContextHolder.getLocale();
        String msg = messageSource.getMessage("common.bad-request", null, "common.bad-request", locale);

        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                msg,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: path={}", request.getRequestURI(), ex);
        var locale = LocaleContextHolder.getLocale();

        ApiError body = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                messageSource.getMessage("common.internal-error", null, "common.internal-error", locale),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
