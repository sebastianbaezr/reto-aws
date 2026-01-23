package co.com.bancolombia.lambda.mapper;

import co.com.bancolombia.lambda.dto.UserDto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class UserLambdaDtoMapper {
    private static final Gson gson = new Gson();

    public static UserDto fromJson(String json) {
        return gson.fromJson(json, UserDto.class);
    }

    public static String toJson(UserDto user) {
        return gson.toJson(user);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        return error;
    }

    public static JsonObject createMessageResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("message", message);
        return response;
    }
}
