package co.com.bancolombia.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonRequest {

    @NotBlank(message = "The identification cannot be empty")
    @Pattern(regexp = "^[0-9]+$", message = "The identification must contain only numbers")
    private String identification;

    @NotBlank(message = "The name cannot be empty")
    private String name;

    @NotBlank(message = "The email cannot be empty")
    @Email(message = "The email must be valid")
    private String email;
}
