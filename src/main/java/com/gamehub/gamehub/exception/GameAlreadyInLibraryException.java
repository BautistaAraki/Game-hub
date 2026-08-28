package com.gamehub.gamehub.exception;

public class GameAlreadyInLibraryException extends RuntimeException {

    public GameAlreadyInLibraryException() {
        super("El juego ya está en la biblioteca del usuario");
    }
}

