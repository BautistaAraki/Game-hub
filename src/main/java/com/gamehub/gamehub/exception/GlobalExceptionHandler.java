package com.gamehub.gamehub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(
            ResourceNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(GameAlreadyInLibraryException.class)
    public ResponseEntity<String> handleGameAlreadyInLibrary(
            GameAlreadyInLibraryException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(
            UserAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ExternalAccountAlreadyLinkedException.class)
    public ResponseEntity<String> handleExternalAccountAlreadyLinked(
            ExternalAccountAlreadyLinkedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(SteamIntegrationException.class)
    public ResponseEntity<String> handleSteamIntegration(
            SteamIntegrationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Parametro invalido: " + exception.getName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Solicitud invalida");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message);
    }
}
