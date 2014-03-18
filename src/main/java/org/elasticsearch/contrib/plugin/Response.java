package org.elasticsearch.contrib.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Response {

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    @JsonProperty
    private String status;
    @JsonProperty
    private String message;
    @JsonProperty
    private List<String> errors;

    private void setFailureResponse(String message) {
        this.message = message;
        status = FAILURE;
    }

    private void setSuccessResponse() {
        status = SUCCESS;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @JsonIgnore
    public String getMessage() {
        return message;
    }

    @JsonIgnore
    public List<String> getErrors() {
        return errors;
    }

    public static Response successfulResponse() {
        Response response = new Response();
        response.setSuccessResponse();
        return response;
    }

    public static Response failureResponse() {
        Response response = new Response();
        response.setFailureResponse(null);
        return response;
    }


    public static Response failureResponse(String message) {
        Response response = new Response();
        response.setFailureResponse(message);
        return response;
    }


    public String toJson() {
        return Serializer.toJson(this);
    }

    public boolean isSuccessful() {
        return SUCCESS.equals(status);
    }
}
