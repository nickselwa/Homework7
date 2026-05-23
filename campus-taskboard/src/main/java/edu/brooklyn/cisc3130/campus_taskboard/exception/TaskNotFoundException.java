package edu.brooklyn.cisc3130.campus_taskboard.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Integer id) {
        super("Task with ID " + id + " not found");
    }
}