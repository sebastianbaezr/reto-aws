package co.com.bancolombia.lambda.service;

import co.com.bancolombia.lambda.model.User;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public interface SqsMessagePublisher {
    SendMessageResponse publishUserCreatedEvent(User user, String requestId);
}
