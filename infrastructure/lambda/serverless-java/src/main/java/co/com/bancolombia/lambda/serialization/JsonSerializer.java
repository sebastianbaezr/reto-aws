package co.com.bancolombia.lambda.serialization;

import com.google.gson.Gson;

public class JsonSerializer {

    private final Gson gson;

    public JsonSerializer() {
        this.gson = new Gson();
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public String toJson(Object object) {
        return gson.toJson(object);
    }
}
