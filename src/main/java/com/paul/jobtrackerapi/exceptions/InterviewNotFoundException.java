package com.paul.jobtrackerapi.exceptions;

public class InterviewNotFoundException extends RuntimeException {
  public InterviewNotFoundException(String message) {
    super(message);
  }
}
