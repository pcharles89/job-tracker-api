package com.paul.jobtrackerapi.exceptions;

public class JobApplicationNotFoundException extends RuntimeException {
    public JobApplicationNotFoundException(Long id) {
        super("Job application with id " + id + " not found");
    }
}
