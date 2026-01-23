package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

public abstract class AbstractLambdaHandler implements RequestHandler<Map<String, Object>, LambdaResponse> {

    protected final JsonSerializer jsonSerializer;
    protected final ResponseFactory responseFactory;

    protected AbstractLambdaHandler(JsonSerializer jsonSerializer, ResponseFactory responseFactory) {
        this.jsonSerializer = jsonSerializer;
        this.responseFactory = responseFactory;
    }

    @Override
    public final LambdaResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            return processRequest(input, context);
        } catch (JsonSyntaxException e) {
            context.getLogger().log("Invalid JSON: " + e.getMessage());
            return new LambdaResponse(400,
                    jsonSerializer.toJson(responseFactory.createError("Invalid JSON format")));
        } catch (LambdaException e) {
            context.getLogger().log("Lambda exception: " + e.getMessage());
            return new LambdaResponse(e.getStatusCode(),
                    jsonSerializer.toJson(responseFactory.createError(e.getMessage())));
        } catch (Exception e) {
            context.getLogger().log("Unexpected error: " + e.getMessage());
            return new LambdaResponse(500,
                    jsonSerializer.toJson(responseFactory.createError("Internal server error")));
        }
    }

    protected abstract LambdaResponse processRequest(Map<String, Object> input, Context context);

    protected Long extractUserId(Map<String, Object> input) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");

        if (pathParameters == null || !pathParameters.containsKey("id")) {
            throw new LambdaException("User ID is required", 400);
        }

        try {
            return Long.parseLong(pathParameters.get("id"));
        } catch (NumberFormatException e) {
            throw new LambdaException("User ID must be a number", 400);
        }
    }

    protected String extractBody(Map<String, Object> input) {
        String body = (String) input.get("body");
        if (body == null || body.isEmpty()) {
            throw new LambdaException("Request body is required", 400);
        }
        return body;
    }
}
