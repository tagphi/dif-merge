package com.rtbasia.difmerge.http;

import org.springframework.http.HttpStatus;

public class SubmitResponse {
    private String message;
    private HttpStatus statusCode;

    public SubmitResponse(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public static SubmitResponse ok() {
        return new SubmitResponse("success", HttpStatus.OK);
    }

    public static SubmitResponse error(String message) {
        return new SubmitResponse(message, HttpStatus.BAD_REQUEST);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }
}
