package com.gamehub.gamehub.exception;

import com.gamehub.gamehub.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(GameAlreadyInLibraryException.class)
    public ResponseEntity<ApiErrorResponse> handleGameAlreadyInLibrary(
            GameAlreadyInLibraryException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(ExternalAccountAlreadyLinkedException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalAccountAlreadyLinked(
            ExternalAccountAlreadyLinkedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(SteamIntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleSteamIntegration(
            SteamIntegrationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Parametro invalido: " + exception.getName(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Solicitud invalida");

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(status)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }
}
