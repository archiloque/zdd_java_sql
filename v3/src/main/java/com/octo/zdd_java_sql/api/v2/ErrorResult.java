package com.octo.zdd_java_sql.api.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;

public class ErrorResult {

    private int code;

    private String message;

    public ErrorResult() {
    }

    public ErrorResult(Response.Status status, String message) {
        this.code = status.getStatusCode();
        this.message = message;
    }

    @JsonProperty
    public long getCode() {
        return code;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }
}
