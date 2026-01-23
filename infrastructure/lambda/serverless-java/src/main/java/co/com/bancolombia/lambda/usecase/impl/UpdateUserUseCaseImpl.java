package co.com.bancolombia.lambda.usecase.impl;

import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.exception.ValidationException;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.repository.UserRepository;
import co.com.bancolombia.lambda.usecase.UpdateUserUseCase;

public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private final UserRepository repository;

    public UpdateUserUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(String userId, User user) {
        repository.findById(userId)
                .orElseThrow(() -> new LambdaException("User not found with ID: " + userId, 404));

        if (repository.emailExistsExcept(user.getEmail(), userId)) {
            throw new ValidationException("Email already exists: " + user.getEmail());
        }

        return repository.update(userId, user);
    }
}
