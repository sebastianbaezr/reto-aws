package co.com.bancolombia.lambda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LambdaRequest {
    private String body;
    private Map<String, String> pathParameters;
    private Map<String, String> queryStringParameters;
    private Map<String, String> headers;
}
