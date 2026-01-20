package co.com.bancolombia.usecase.person.createperson;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.PersonErrorCode;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import co.com.bancolombia.usecase.validator.PersonValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePersonUseCaseTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonValidator personValidator;

    private CreatePersonUseCase createPersonUseCase;

    @BeforeEach
    void setUp() {
        createPersonUseCase = new CreatePersonUseCase(personRepository, personValidator);
    }

    @Test
    void shouldCreatePersonSuccessfully() {
        // Arrange
        Person personToCreate = Person.builder()
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        Person savedPerson = Person.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        doNothing().when(personValidator).validateIdentificationNotExists("12345");
        doNothing().when(personValidator).validateEmailNotExists("john@example.com");
        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

        // Act
        Person result = createPersonUseCase.execute(personToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345", result.getIdentification());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(personValidator).validateIdentificationNotExists("12345");
        verify(personValidator).validateEmailNotExists("john@example.com");
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void shouldThrowExceptionWhenIdentificationAlreadyExists() {
        // Arrange
        Person personToCreate = Person.builder()
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        doThrow(new BusinessException(PersonErrorCode.IDENTIFICATION_ALREADY_EXISTS))
                .when(personValidator)
                .validateIdentificationNotExists("12345");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            createPersonUseCase.execute(personToCreate);
        });

        assertEquals(PersonErrorCode.IDENTIFICATION_ALREADY_EXISTS.getCode(), exception.getCode());
        assertEquals(PersonErrorCode.IDENTIFICATION_ALREADY_EXISTS.getMessage(), exception.getMessage());

        verify(personValidator).validateIdentificationNotExists("12345");
        verify(personValidator, never()).validateEmailNotExists(anyString());
        verify(personRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        Person personToCreate = Person.builder()
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        doNothing().when(personValidator).validateIdentificationNotExists("12345");
        doThrow(new BusinessException(PersonErrorCode.EMAIL_ALREADY_EXISTS))
                .when(personValidator)
                .validateEmailNotExists("john@example.com");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            createPersonUseCase.execute(personToCreate);
        });

        assertEquals(PersonErrorCode.EMAIL_ALREADY_EXISTS.getCode(), exception.getCode());
        assertEquals(PersonErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), exception.getMessage());

        verify(personValidator).validateIdentificationNotExists("12345");
        verify(personValidator).validateEmailNotExists("john@example.com");
        verify(personRepository, never()).save(any());
    }

    @Test
    void shouldSetIdToNullBeforeSaving() {
        // Arrange
        Person personToCreate = Person.builder()
                .id(999L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        Person savedPerson = Person.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        doNothing().when(personValidator).validateIdentificationNotExists("12345");
        doNothing().when(personValidator).validateEmailNotExists("john@example.com");
        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

        // Act
        Person result = createPersonUseCase.execute(personToCreate);

        // Assert
        assertNull(personToCreate.getId());
        assertEquals(1L, result.getId());

        verify(personRepository).save(any(Person.class));
    }
}
