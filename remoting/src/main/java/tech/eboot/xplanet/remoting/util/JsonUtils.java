package tech.eboot.xplanet.remoting.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class JsonUtils
{
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static byte[] toByteArray(Object obj)
    {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object obj)
    {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readObject(String json, Type type)
    {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readObject(byte[] body, Type type)
    {
        return readObject(new String(body, StandardCharsets.UTF_8), type);
    }

    public static void printJson(Object obj)
    {
        System.out.println(toJson(obj));
    }
}
