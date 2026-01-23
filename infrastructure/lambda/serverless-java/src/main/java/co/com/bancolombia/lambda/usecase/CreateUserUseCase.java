package co.com.bancolombia.lambda.usecase;

import co.com.bancolombia.lambda.model.User;

public interface CreateUserUseCase {
    User execute(User user);
}
