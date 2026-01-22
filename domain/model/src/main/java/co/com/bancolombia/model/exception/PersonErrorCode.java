package co.com.bancolombia.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PersonErrorCode {
    PERSON_NOT_FOUND("PERSON_NOT_FOUND", "Persona no encontrada"),
    IDENTIFICATION_ALREADY_EXISTS("IDENTIFICATION_ALREADY_EXISTS", "Ya existe una persona con este número de identificacion"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Ya existe una persona con este correo electronico");

    private final String code;
    private final String message;
}
