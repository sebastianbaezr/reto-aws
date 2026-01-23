package co.com.bancolombia.lambda.usecase;

import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.repository.InMemoryUserRepository;

public class DeleteUserLambdaUseCase {
    private final InMemoryUserRepository repository;

    public DeleteUserLambdaUseCase(InMemoryUserRepository repository) {
        this.repository = repository;
    }

    public void execute(Long userId) {
        // Verify user exists
        repository.findById(userId)
                .orElseThrow(() -> new LambdaException("User not found with ID: " + userId, 404));

        repository.delete(userId);
    }
}
