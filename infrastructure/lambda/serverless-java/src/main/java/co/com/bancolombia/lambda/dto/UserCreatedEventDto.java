package co.com.bancolombia.lambda.dto;

import co.com.bancolombia.lambda.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEventDto {
    private String eventType;
    private String timestamp;
    private User data;
    private Map<String, String> metadata;
}
