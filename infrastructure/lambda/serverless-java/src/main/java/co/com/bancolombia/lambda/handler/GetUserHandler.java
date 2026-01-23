package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.factory.ServiceFactory;
import co.com.bancolombia.lambda.mapper.UserMapper;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.usecase.GetUserUseCase;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.Map;

public class GetUserHandler extends AbstractLambdaHandler {

    private final GetUserUseCase getUserUseCase;
    private final UserMapper userMapper;

    public GetUserHandler() {
        super(
                ServiceFactory.getJsonSerializer(),
                ServiceFactory.getResponseFactory()
        );
        this.getUserUseCase = ServiceFactory.getGetUserUseCase();
        this.userMapper = ServiceFactory.getUserMapper();
    }

    GetUserHandler(JsonSerializer jsonSerializer,
                   ResponseFactory responseFactory,
                   GetUserUseCase getUserUseCase,
                   UserMapper userMapper) {
        super(jsonSerializer, responseFactory);
        this.getUserUseCase = getUserUseCase;
        this.userMapper = userMapper;
    }

    @Override
    protected LambdaResponse processRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Getting user by ID");

        String userId = extractUserId(input);
        User user = getUserUseCase.execute(userId);

        return new LambdaResponse(200,
                jsonSerializer.toJson(userMapper.modelToResponse(user)));
    }
}