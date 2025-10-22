package com.rifushigi.stringly.exception;

public class BadQueryException extends RuntimeException {
    public BadQueryException(String message){
        super(message);
    }
}
