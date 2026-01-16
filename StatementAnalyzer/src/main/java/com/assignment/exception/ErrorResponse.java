package com.assignment.exception;

import java.util.HashMap;

public class ErrorResponse {
    private HashMap<String,String> errors;

    public ErrorResponse() {}
    public ErrorResponse(HashMap<String,String> errors) {
        this.errors = errors;
    }
}
