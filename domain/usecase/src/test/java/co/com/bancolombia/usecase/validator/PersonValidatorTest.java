package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.PersonErrorCode;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonValidatorTest {

    @Mock
    private PersonRepository personRepository;

    private PersonValidator personValidator;

    @BeforeEach
    void setUp() {
        personValidator = new PersonValidator(personRepository);
    }

    @Test
    void shouldNotThrowExceptionWhenIdentificationDoesNotExist() {
        // Arrange
        String identification = "12345";
        when(personRepository.findByIdentification(identification)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> personValidator.validateIdentificationNotExists(identification));
    }

    @Test
    void shouldThrowExceptionWhenIdentificationAlreadyExists() {
        // Arrange
        String identification = "12345";
        Person existingPerson = Person.builder()
                .id(1L)
                .identification(identification)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(personRepository.findByIdentification(identification)).thenReturn(existingPerson);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            personValidator.validateIdentificationNotExists(identification);
        });

        assertEquals(PersonErrorCode.IDENTIFICATION_ALREADY_EXISTS.getCode(), exception.getCode());
        assertEquals(PersonErrorCode.IDENTIFICATION_ALREADY_EXISTS.getMessage(), exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionWhenEmailDoesNotExist() {
        // Arrange
        String email = "john@example.com";
        when(personRepository.findByEmail(email)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> personValidator.validateEmailNotExists(email));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        String email = "john@example.com";
        Person existingPerson = Person.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email(email)
                .build();

        when(personRepository.findByEmail(email)).thenReturn(existingPerson);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            personValidator.validateEmailNotExists(email);
        });

        assertEquals(PersonErrorCode.EMAIL_ALREADY_EXISTS.getCode(), exception.getCode());
        assertEquals(PersonErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), exception.getMessage());
    }
}
