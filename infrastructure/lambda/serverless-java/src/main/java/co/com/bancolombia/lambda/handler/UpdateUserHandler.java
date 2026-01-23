package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.dto.UserDto;
import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.mapper.UserLambdaDtoMapper;
import co.com.bancolombia.lambda.repository.InMemoryUserRepository;
import co.com.bancolombia.lambda.usecase.UpdateUserLambdaUseCase;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

public class UpdateUserHandler implements RequestHandler<Map<String, Object>, LambdaResponse> {

    private final InMemoryUserRepository repository = new InMemoryUserRepository();
    private final UpdateUserLambdaUseCase useCase = new UpdateUserLambdaUseCase(repository);

    @Override
    public LambdaResponse handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Updating user");

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");

            if (pathParameters == null || !pathParameters.containsKey("id")) {
                return new LambdaResponse(400, UserLambdaDtoMapper.toJson(
                        UserLambdaDtoMapper.createErrorResponse("User ID is required")));
            }

            Long userId = Long.parseLong(pathParameters.get("id"));

            String body = (String) input.get("body");
            if (body == null || body.isEmpty()) {
                return new LambdaResponse(400, UserLambdaDtoMapper.toJson(
                        UserLambdaDtoMapper.createErrorResponse("Request body is required")));
            }

            UserDto userInput = UserLambdaDtoMapper.fromJson(body);
            UserDto updatedUser = useCase.execute(userId, userInput);

            return new LambdaResponse(200, UserLambdaDtoMapper.toJson(updatedUser));
        } catch (NumberFormatException e) {
            context.getLogger().log("Invalid user ID format: " + e.getMessage());
            return new LambdaResponse(400, UserLambdaDtoMapper.toJson(
                    UserLambdaDtoMapper.createErrorResponse("User ID must be a number")));
        } catch (JsonSyntaxException e) {
            context.getLogger().log("Invalid JSON: " + e.getMessage());
            return new LambdaResponse(400, UserLambdaDtoMapper.toJson(
                    UserLambdaDtoMapper.createErrorResponse("Invalid JSON format")));
        } catch (LambdaException e) {
            context.getLogger().log("Lambda exception: " + e.getMessage());
            return new LambdaResponse(e.getStatusCode(), UserLambdaDtoMapper.toJson(
                    UserLambdaDtoMapper.createErrorResponse(e.getMessage())));
        } catch (Exception e) {
            context.getLogger().log("Unexpected error: " + e.getMessage());
            return new LambdaResponse(500, UserLambdaDtoMapper.toJson(
                    UserLambdaDtoMapper.createErrorResponse("Internal server error")));
        }
    }
}
