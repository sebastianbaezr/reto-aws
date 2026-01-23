package co.com.bancolombia.lambda.response;

import co.com.bancolombia.lambda.dto.ErrorResponseDto;
import co.com.bancolombia.lambda.dto.MessageResponseDto;

public class ResponseFactory {

    public ErrorResponseDto createError(String message) {
        return ErrorResponseDto.builder()
                .error(message)
                .build();
    }

    public MessageResponseDto createMessage(String message) {
        return MessageResponseDto.builder()
                .message(message)
                .build();
    }
}
