package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.dto.UserRequestDto;
import co.com.bancolombia.lambda.factory.ServiceFactory;
import co.com.bancolombia.lambda.mapper.UserMapper;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.usecase.UpdateUserUseCase;
import co.com.bancolombia.lambda.validation.ValidationService;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.Map;

public class UpdateUserHandler extends AbstractLambdaHandler {

    private final UpdateUserUseCase updateUserUseCase;
    private final UserMapper userMapper;
    private final ValidationService validationService;

    public UpdateUserHandler() {
        super(
                ServiceFactory.getJsonSerializer(),
                ServiceFactory.getResponseFactory()
        );
        this.updateUserUseCase = ServiceFactory.getUpdateUserUseCase();
        this.userMapper = ServiceFactory.getUserMapper();
        this.validationService = ServiceFactory.getValidationService();
    }

    UpdateUserHandler(JsonSerializer jsonSerializer,
                      ResponseFactory responseFactory,
                      UpdateUserUseCase updateUserUseCase,
                      UserMapper userMapper,
                      ValidationService validationService) {
        super(jsonSerializer, responseFactory);
        this.updateUserUseCase = updateUserUseCase;
        this.userMapper = userMapper;
        this.validationService = validationService;
    }

    @Override
    protected LambdaResponse processRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Updating user");

        Long userId = extractUserId(input);
        String body = extractBody(input);

        UserRequestDto requestDto = jsonSerializer.fromJson(body, UserRequestDto.class);
        validationService.validate(requestDto);

        User user = userMapper.requestToModel(requestDto);
        User updatedUser = updateUserUseCase.execute(userId, user);

        return new LambdaResponse(200,
                jsonSerializer.toJson(userMapper.modelToResponse(updatedUser)));
    }
}