package co.com.bancolombia.lambda.usecase;

import co.com.bancolombia.lambda.dto.UserDto;
import co.com.bancolombia.lambda.exception.ValidationException;
import co.com.bancolombia.lambda.repository.InMemoryUserRepository;
import java.util.regex.Pattern;

public class CreateUserLambdaUseCase {
    private final InMemoryUserRepository repository;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public CreateUserLambdaUseCase(InMemoryUserRepository repository) {
        this.repository = repository;
    }

    public UserDto execute(UserDto user) {
        validateUser(user);

        if (repository.emailExists(user.getEmail())) {
            throw new ValidationException("Email already exists: " + user.getEmail());
        }

        return repository.create(user);
    }

    private void validateUser(UserDto user) {
        if (user == null) {
            throw new ValidationException("User cannot be null");
        }
        if (user.getNombre() == null || user.getNombre().trim().isEmpty()) {
            throw new ValidationException("Nombre is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }
}
