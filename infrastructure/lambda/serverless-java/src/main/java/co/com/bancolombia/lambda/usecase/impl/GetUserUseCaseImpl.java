package co.com.bancolombia.lambda.usecase.impl;

import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.repository.UserRepository;
import co.com.bancolombia.lambda.usecase.GetUserUseCase;

public class GetUserUseCaseImpl implements GetUserUseCase {

    private final UserRepository repository;

    public GetUserUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(String userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new LambdaException("User not found with ID: " + userId, 404));
    }
}
