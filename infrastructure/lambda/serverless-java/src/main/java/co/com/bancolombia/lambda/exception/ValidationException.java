package co.com.bancolombia.lambda.exception;

public class ValidationException extends LambdaException {
    public ValidationException(String message) {
        super(message, 400);
    }
}
