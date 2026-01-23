package co.com.bancolombia.lambda.usecase;

import co.com.bancolombia.lambda.model.User;

public interface UpdateUserUseCase {
    User execute(Long userId, User user);
}
