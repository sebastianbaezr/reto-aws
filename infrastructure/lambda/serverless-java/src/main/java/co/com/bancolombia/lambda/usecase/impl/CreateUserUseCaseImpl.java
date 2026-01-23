package co.com.bancolombia.lambda.usecase.impl;

import co.com.bancolombia.lambda.exception.ValidationException;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.repository.UserRepository;
import co.com.bancolombia.lambda.usecase.CreateUserUseCase;

public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepository repository;

    public CreateUserUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(User user) {
        if (repository.emailExists(user.getEmail())) {
            throw new ValidationException("Email already exists: " + user.getEmail());
        }

        return repository.create(user);
    }
}
