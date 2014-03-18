package org.elasticsearch.contrib.plugin;

import org.json.simple.JSONObject;

public class Response extends JSONObject {

    enum Status {
        SUCCESS, FAILURE
    }

    private Response() {
    }

    public static Response successfulResponse(String correlationId) {
        Response response = successfulResponse();
        response.put("correlation-id", correlationId);
        return response;
    }


    public static Response successfulResponse() {
        Response response = new Response();
        response.put("status", Status.SUCCESS.toString());
        return response;
    }
}
