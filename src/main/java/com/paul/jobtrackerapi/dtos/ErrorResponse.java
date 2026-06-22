package com.paul.jobtrackerapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private int status;
    Map<String, String> errors;
}
