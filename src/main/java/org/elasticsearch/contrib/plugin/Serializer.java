package org.elasticsearch.contrib.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

public class Serializer {

    public static String toJson(Object objectToSerialize) {
        StringWriter stringWriter = new StringWriter();
        try {
            ObjectMapper objectMapper = getObjectMapper();


            objectMapper.writeValue(stringWriter, objectToSerialize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        T serializedObject = null;
        try {
            serializedObject = getObjectMapper().readValue(jsonString, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serializedObject;
    }

    public static JSONObject toJsonObject(Object objectToJsonize) {
        return fromJson(toJson(objectToJsonize), JSONObject.class);
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }
}
