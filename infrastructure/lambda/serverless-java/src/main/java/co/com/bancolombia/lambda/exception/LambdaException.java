package co.com.bancolombia.lambda.exception;

public class LambdaException extends RuntimeException {
    private final int statusCode;

    public LambdaException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public LambdaException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
