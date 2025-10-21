package com.rifushigi.stringly.exception;

public class StringAlreadyExistsException extends RuntimeException{
    public StringAlreadyExistsException(String message){
        super(message);
    }
}
