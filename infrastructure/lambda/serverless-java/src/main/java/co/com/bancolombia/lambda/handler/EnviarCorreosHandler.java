package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.UserCreatedEventDto;
import co.com.bancolombia.lambda.factory.ServiceFactory;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.service.SnsEmailService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;

import java.util.ArrayList;
import java.util.List;

public class EnviarCorreosHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private final SnsEmailService snsEmailService;
    private final JsonSerializer jsonSerializer;

    public EnviarCorreosHandler() {
        this.snsEmailService = ServiceFactory.getSnsEmailService();
        this.jsonSerializer = ServiceFactory.getJsonSerializer();
    }

    EnviarCorreosHandler(SnsEmailService snsEmailService, JsonSerializer jsonSerializer) {
        this.snsEmailService = snsEmailService;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        context.getLogger().log("Processing " + event.getRecords().size() + " messages from SQS");

        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();

        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                processMessage(message, context);
            } catch (Exception e) {
                context.getLogger().log("Failed to process message: " + message.getMessageId() +
                        " - Error: " + e.getMessage());
                failures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }
        }

        return new SQSBatchResponse(failures);
    }

    private void processMessage(SQSEvent.SQSMessage message, Context context) {
        String messageBody = message.getBody();
        context.getLogger().log("Processing message: " + message.getMessageId());

        UserCreatedEventDto event = jsonSerializer.fromJson(messageBody, UserCreatedEventDto.class);

        if (event.getEventType() == null) {
            throw new IllegalArgumentException("Event type cannot be null for message: " + message.getMessageId());
        }

        if (!"USER_CREATED".equals(event.getEventType())) {
            context.getLogger().log("Ignoring event type: " + event.getEventType() + " for message: " + message.getMessageId());
            return;
        }

        User user = event.getData();
        if (user == null || user.getEmail() == null) {
            throw new IllegalArgumentException("User data is invalid for message: " + message.getMessageId());
        }

        context.getLogger().log("Sending welcome email to: " + user.getEmail());
        snsEmailService.sendWelcomeEmail(user);
        context.getLogger().log("Successfully processed message for user: " + user.getId());
    }
}
