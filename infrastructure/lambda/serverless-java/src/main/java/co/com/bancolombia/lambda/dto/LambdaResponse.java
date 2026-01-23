package co.com.bancolombia.lambda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LambdaResponse {
    private int statusCode;
    private String body;
    private boolean isBase64Encoded;

    public LambdaResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        this.isBase64Encoded = false;
    }
}
