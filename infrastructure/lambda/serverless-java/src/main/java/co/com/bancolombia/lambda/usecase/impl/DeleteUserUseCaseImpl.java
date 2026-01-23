package co.com.bancolombia.lambda.usecase.impl;

import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.repository.UserRepository;
import co.com.bancolombia.lambda.usecase.DeleteUserUseCase;

public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    private final UserRepository repository;

    public DeleteUserUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long userId) {
        repository.findById(userId)
                .orElseThrow(() -> new LambdaException("User not found with ID: " + userId, 404));

        repository.delete(userId);
    }
}
