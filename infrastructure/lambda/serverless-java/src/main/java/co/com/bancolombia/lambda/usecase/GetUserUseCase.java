package co.com.bancolombia.lambda.usecase;

import co.com.bancolombia.lambda.model.User;

public interface GetUserUseCase {
    User execute(Long userId);
}
