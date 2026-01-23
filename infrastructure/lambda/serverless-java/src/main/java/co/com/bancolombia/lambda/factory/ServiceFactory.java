package co.com.bancolombia.lambda.factory;

import co.com.bancolombia.lambda.mapper.UserMapper;
import co.com.bancolombia.lambda.mapper.UserMapperImpl;
import co.com.bancolombia.lambda.repository.InMemoryUserRepository;
import co.com.bancolombia.lambda.repository.UserRepository;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.usecase.CreateUserUseCase;
import co.com.bancolombia.lambda.usecase.DeleteUserUseCase;
import co.com.bancolombia.lambda.usecase.GetUserUseCase;
import co.com.bancolombia.lambda.usecase.UpdateUserUseCase;
import co.com.bancolombia.lambda.usecase.impl.CreateUserUseCaseImpl;
import co.com.bancolombia.lambda.usecase.impl.DeleteUserUseCaseImpl;
import co.com.bancolombia.lambda.usecase.impl.GetUserUseCaseImpl;
import co.com.bancolombia.lambda.usecase.impl.UpdateUserUseCaseImpl;
import co.com.bancolombia.lambda.validation.ValidationService;

public class ServiceFactory {

    // Singleton instances
    private static final UserRepository USER_REPOSITORY = new InMemoryUserRepository();
    private static final UserMapper USER_MAPPER = new UserMapperImpl();
    private static final ValidationService VALIDATION_SERVICE = new ValidationService();
    private static final JsonSerializer JSON_SERIALIZER = new JsonSerializer();
    private static final ResponseFactory RESPONSE_FACTORY = new ResponseFactory();

    // Use cases
    private static final CreateUserUseCase CREATE_USER_USE_CASE = new CreateUserUseCaseImpl(USER_REPOSITORY);
    private static final GetUserUseCase GET_USER_USE_CASE = new GetUserUseCaseImpl(USER_REPOSITORY);
    private static final UpdateUserUseCase UPDATE_USER_USE_CASE = new UpdateUserUseCaseImpl(USER_REPOSITORY);
    private static final DeleteUserUseCase DELETE_USER_USE_CASE = new DeleteUserUseCaseImpl(USER_REPOSITORY);

    public static UserRepository getUserRepository() {
        return USER_REPOSITORY;
    }

    public static UserMapper getUserMapper() {
        return USER_MAPPER;
    }

    public static ValidationService getValidationService() {
        return VALIDATION_SERVICE;
    }

    public static JsonSerializer getJsonSerializer() {
        return JSON_SERIALIZER;
    }

    public static ResponseFactory getResponseFactory() {
        return RESPONSE_FACTORY;
    }

    public static CreateUserUseCase getCreateUserUseCase() {
        return CREATE_USER_USE_CASE;
    }

    public static GetUserUseCase getGetUserUseCase() {
        return GET_USER_USE_CASE;
    }

    public static UpdateUserUseCase getUpdateUserUseCase() {
        return UPDATE_USER_USE_CASE;
    }

    public static DeleteUserUseCase getDeleteUserUseCase() {
        return DELETE_USER_USE_CASE;
    }
}