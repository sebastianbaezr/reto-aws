package co.com.bancolombia.lambda.service.impl;

import co.com.bancolombia.lambda.dto.UserCreatedEventDto;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.service.SqsMessagePublisher;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SqsMessagePublisherImpl implements SqsMessagePublisher {

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final JsonSerializer jsonSerializer;

    public SqsMessagePublisherImpl(SqsClient sqsClient, String queueUrl, JsonSerializer jsonSerializer) {
        this.sqsClient = Objects.requireNonNull(sqsClient, "sqsClient must not be null");
        this.queueUrl = Objects.requireNonNull(queueUrl, "queueUrl must not be null");
        this.jsonSerializer = Objects.requireNonNull(jsonSerializer, "jsonSerializer must not be null");
    }

    @Override
    public SendMessageResponse publishUserCreatedEvent(User user, String requestId) {
        try {
            UserCreatedEventDto event = UserCreatedEventDto.builder()
                    .eventType("USER_CREATED")
                    .timestamp(Instant.now().toString())
                    .data(user)
                    .metadata(createMetadata(requestId))
                    .build();

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(jsonSerializer.toJson(event))
                    .build();

            return sqsClient.sendMessage(sendMessageRequest);
        } catch (SqsException e) {
            throw new RuntimeException("Failed to publish message to SQS: " + e.getMessage(), e);
        }
    }

    private Map<String, String> createMetadata(String requestId) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "createUserJava");
        metadata.put("version", "1.0");
        metadata.put("requestId", requestId);
        return metadata;
    }
}
