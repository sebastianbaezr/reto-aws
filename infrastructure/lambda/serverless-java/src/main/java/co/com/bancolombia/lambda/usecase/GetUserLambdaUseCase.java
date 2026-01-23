package co.com.bancolombia.lambda.usecase;

import co.com.bancolombia.lambda.dto.UserDto;
import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.repository.InMemoryUserRepository;

public class GetUserLambdaUseCase {
    private final InMemoryUserRepository repository;

    public GetUserLambdaUseCase(InMemoryUserRepository repository) {
        this.repository = repository;
    }

    public UserDto execute(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new LambdaException("User not found with ID: " + userId, 404));
    }
}
