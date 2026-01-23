package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.factory.ServiceFactory;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.usecase.DeleteUserUseCase;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.Map;

public class DeleteUserHandler extends AbstractLambdaHandler {

    private final DeleteUserUseCase deleteUserUseCase;

    public DeleteUserHandler() {
        super(
                ServiceFactory.getJsonSerializer(),
                ServiceFactory.getResponseFactory()
        );
        this.deleteUserUseCase = ServiceFactory.getDeleteUserUseCase();
    }

    DeleteUserHandler(JsonSerializer jsonSerializer,
                      ResponseFactory responseFactory,
                      DeleteUserUseCase deleteUserUseCase) {
        super(jsonSerializer, responseFactory);
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @Override
    protected LambdaResponse processRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Deleting user");

        Long userId = extractUserId(input);
        deleteUserUseCase.execute(userId);

        return new LambdaResponse(200,
                jsonSerializer.toJson(responseFactory.createMessage("User deleted successfully")));
    }
}