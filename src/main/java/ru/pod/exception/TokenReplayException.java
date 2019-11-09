package ru.pod.exception;

public class TokenReplayException extends Exception {
    public TokenReplayException(String message) {
        super(message);
    }
}
