package com.example.exception;
public class JobApplicationExistsException extends RuntimeException {
    public JobApplicationExistsException(String message) { super(message); }
}
