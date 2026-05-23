package edu.brooklyn.cisc3130.campus_taskboard.exception;

public class InvalidTaskDataException extends RuntimeException {

    public InvalidTaskDataException(String message) {
        super(message);
    }
}