package co.com.bancolombia.lambda.factory;

import co.com.bancolombia.lambda.mapper.UserMapper;
import co.com.bancolombia.lambda.repository.DynamoDbUserRepository;
import co.com.bancolombia.lambda.repository.InMemoryUserRepository;
import co.com.bancolombia.lambda.repository.UserRepository;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.service.SnsEmailService;
import co.com.bancolombia.lambda.service.SqsMessagePublisher;
import co.com.bancolombia.lambda.service.impl.SnsEmailServiceImpl;
import co.com.bancolombia.lambda.service.impl.SqsMessagePublisherImpl;
import co.com.bancolombia.lambda.usecase.CreateUserUseCase;
import co.com.bancolombia.lambda.usecase.DeleteUserUseCase;
import co.com.bancolombia.lambda.usecase.GetUserUseCase;
import co.com.bancolombia.lambda.usecase.UpdateUserUseCase;
import co.com.bancolombia.lambda.usecase.impl.CreateUserUseCaseImpl;
import co.com.bancolombia.lambda.usecase.impl.DeleteUserUseCaseImpl;
import co.com.bancolombia.lambda.usecase.impl.GetUserUseCaseImpl;
import co.com.bancolombia.lambda.usecase.impl.UpdateUserUseCaseImpl;
import co.com.bancolombia.lambda.validation.ValidationService;
import org.mapstruct.factory.Mappers;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public class ServiceFactory {

    private static final UserRepository USER_REPOSITORY = initializeRepository();
    private static final UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);
    private static final ValidationService VALIDATION_SERVICE = new ValidationService();
    private static final JsonSerializer JSON_SERIALIZER = new JsonSerializer();
    private static final ResponseFactory RESPONSE_FACTORY = new ResponseFactory();

    private static final CreateUserUseCase CREATE_USER_USE_CASE = new CreateUserUseCaseImpl(USER_REPOSITORY);
    private static final GetUserUseCase GET_USER_USE_CASE = new GetUserUseCaseImpl(USER_REPOSITORY);
    private static final UpdateUserUseCase UPDATE_USER_USE_CASE = new UpdateUserUseCaseImpl(USER_REPOSITORY);
    private static final DeleteUserUseCase DELETE_USER_USE_CASE = new DeleteUserUseCaseImpl(USER_REPOSITORY);

    private static final SqsClient SQS_CLIENT = initializeSqsClient();
    private static final SnsClient SNS_CLIENT = initializeSnsClient();
    private static final String QUEUE_URL = getQueueUrl();
    private static final String TOPIC_ARN = getTopicArn();

    private static final SqsMessagePublisher SQS_MESSAGE_PUBLISHER = new SqsMessagePublisherImpl(SQS_CLIENT, QUEUE_URL, JSON_SERIALIZER);
    private static final SnsEmailService SNS_EMAIL_SERVICE = new SnsEmailServiceImpl(SNS_CLIENT, TOPIC_ARN);

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

    public static SqsMessagePublisher getSqsMessagePublisher() {
        return SQS_MESSAGE_PUBLISHER;
    }

    public static SnsEmailService getSnsEmailService() {
        return SNS_EMAIL_SERVICE;
    }

    private static UserRepository initializeRepository() {
        String dynamoDbTableName = System.getenv("DYNAMODB_TABLE_NAME");
        if (dynamoDbTableName != null && !dynamoDbTableName.isEmpty()) {
            return new DynamoDbUserRepository();
        }
        return new InMemoryUserRepository();
    }

    private static SqsClient initializeSqsClient() {
        return SqsClient.builder().build();
    }

    private static SnsClient initializeSnsClient() {
        return SnsClient.builder().build();
    }

    private static String getQueueUrl() {
        String queueUrl = System.getenv("USER_CREATED_QUEUE_URL");
        if (queueUrl == null || queueUrl.isEmpty()) {
            throw new IllegalStateException("USER_CREATED_QUEUE_URL environment variable must be set");
        }
        return queueUrl;
    }

    private static String getTopicArn() {
        String topicArn = System.getenv("EMAIL_NOTIFICATION_TOPIC_ARN");
        if (topicArn == null || topicArn.isEmpty()) {
            throw new IllegalStateException("EMAIL_NOTIFICATION_TOPIC_ARN environment variable must be set");
        }
        return topicArn;
    }
}