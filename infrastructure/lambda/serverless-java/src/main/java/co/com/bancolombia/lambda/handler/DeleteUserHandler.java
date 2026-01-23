package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.mapper.UserLambdaDtoMapper;
import co.com.bancolombia.lambda.repository.InMemoryUserRepository;
import co.com.bancolombia.lambda.usecase.DeleteUserLambdaUseCase;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public class DeleteUserHandler implements RequestHandler<Map<String, Object>, LambdaResponse> {

    private final InMemoryUserRepository repository = new InMemoryUserRepository();
    private final DeleteUserLambdaUseCase useCase = new DeleteUserLambdaUseCase(repository);

    @Override
    public LambdaResponse handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Deleting user");

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");

            if (pathParameters == null || !pathParameters.containsKey("id")) {
                return new LambdaResponse(400, UserLambdaDtoMapper.toJson(
                        UserLambdaDtoMapper.createErrorResponse("User ID is required")));
            }

            Long userId = Long.parseLong(pathParameters.get("id"));
            useCase.execute(userId);

            return new LambdaResponse(200, UserLambdaDtoMapper.toJson(
                    UserLambdaDtoMapper.createMessageResponse("User deleted successfully")));
        } catch (NumberFormatException e) {
            context.getLogger().log("Invalid user ID format: " + e.getMessage());
            return new LambdaResponse(400, UserLambdaDtoMapper.toJson(
                    UserLambdaDtoMapper.createErrorResponse("User ID must be a number")));
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
