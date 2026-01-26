package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.dto.UserRequestDto;
import co.com.bancolombia.lambda.factory.ServiceFactory;
import co.com.bancolombia.lambda.mapper.UserMapper;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.service.SqsMessagePublisher;
import co.com.bancolombia.lambda.usecase.CreateUserUseCase;
import co.com.bancolombia.lambda.validation.ValidationService;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.Map;

public class CreateUserHandler extends AbstractLambdaHandler {

    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;
    private final ValidationService validationService;
    private final SqsMessagePublisher sqsMessagePublisher;

    public CreateUserHandler() {
        super(
                ServiceFactory.getJsonSerializer(),
                ServiceFactory.getResponseFactory()
        );
        this.createUserUseCase = ServiceFactory.getCreateUserUseCase();
        this.userMapper = ServiceFactory.getUserMapper();
        this.validationService = ServiceFactory.getValidationService();
        this.sqsMessagePublisher = ServiceFactory.getSqsMessagePublisher();
    }

    CreateUserHandler(JsonSerializer jsonSerializer,
                      ResponseFactory responseFactory,
                      CreateUserUseCase createUserUseCase,
                      UserMapper userMapper,
                      ValidationService validationService,
                      SqsMessagePublisher sqsMessagePublisher) {
        super(jsonSerializer, responseFactory);
        this.createUserUseCase = createUserUseCase;
        this.userMapper = userMapper;
        this.validationService = validationService;
        this.sqsMessagePublisher = sqsMessagePublisher;
    }

    @Override
    protected LambdaResponse processRequest(Map<String, Object> input, Context context) {
        String requestId = context.getAwsRequestId();
        context.getLogger().log("Creating new user - RequestId: " + requestId);

        String body = extractBody(input);
        UserRequestDto requestDto = jsonSerializer.fromJson(body, UserRequestDto.class);

        validationService.validate(requestDto);

        User user = userMapper.requestToModel(requestDto);
        User createdUser = createUserUseCase.execute(user);

        try {
            sqsMessagePublisher.publishUserCreatedEvent(createdUser, requestId);
            context.getLogger().log("User created event published to SQS for user: " + createdUser.getId());
        } catch (Exception e) {
            context.getLogger().log("WARNING: Failed to publish SQS message: " + e.getMessage());
        }

        return new LambdaResponse(201,
                jsonSerializer.toJson(userMapper.modelToResponse(createdUser)));
    }
}