package co.com.bancolombia.lambda.service;

import co.com.bancolombia.lambda.model.User;

public interface SnsEmailService {
    void sendWelcomeEmail(User user);
}
