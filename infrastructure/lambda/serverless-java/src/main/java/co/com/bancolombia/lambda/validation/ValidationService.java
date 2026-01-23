package co.com.bancolombia.lambda.validation;

import co.com.bancolombia.lambda.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationService {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException(message);
        }
    }
}