package co.com.bancolombia.lambda.service.impl;

import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.service.SnsEmailService;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SnsEmailServiceImpl implements SnsEmailService {

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsEmailServiceImpl(SnsClient snsClient, String topicArn) {
        this.snsClient = Objects.requireNonNull(snsClient, "snsClient must not be null");
        this.topicArn = Objects.requireNonNull(topicArn, "topicArn must not be null");
    }

    @Override
    public void sendWelcomeEmail(User user) {
        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject("Welcome to Our Platform!")
                    .message(buildWelcomeMessage(user))
                    .messageAttributes(buildMessageAttributes(user))
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);
            System.out.println("Email notification sent. MessageId: " + response.messageId());
        } catch (SnsException e) {
            throw new RuntimeException("Failed to send email via SNS: " + e.getMessage(), e);
        }
    }

    private String buildWelcomeMessage(User user) {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to our platform! Your account has been successfully created.\n\n" +
            "Email: %s\n" +
            "User ID: %s\n\n" +
            "Best regards,\n" +
            "The Team",
            user.getNombre(),
            user.getEmail(),
            user.getId()
        );
    }

    private Map<String, MessageAttributeValue> buildMessageAttributes(User user) {
        Map<String, MessageAttributeValue> attributes = new HashMap<>();

        attributes.put("userId", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(user.getId())
                .build());

        attributes.put("email", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(user.getEmail())
                .build());

        return attributes;
    }
}
