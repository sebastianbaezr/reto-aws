package co.com.bancolombia.usecase.person.getperson;

import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPersonUseCaseTest {

    @Mock
    private PersonRepository personRepository;

    private GetPersonUseCase getPersonUseCase;

    @BeforeEach
    void setUp() {
        getPersonUseCase = new GetPersonUseCase(personRepository);
    }

    @Test
    void shouldGetPersonByIdSuccessfully() {
        // Arrange
        Long personId = 1L;
        Person expectedPerson = Person.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(personRepository.findById(personId)).thenReturn(expectedPerson);

        // Act
        Person result = getPersonUseCase.execute(personId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345", result.getIdentification());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(personRepository).findById(personId);
    }

    @Test
    void shouldReturnNullWhenPersonDoesNotExist() {
        // Arrange
        Long personId = 999L;

        when(personRepository.findById(personId)).thenReturn(null);

        // Act
        Person result = getPersonUseCase.execute(personId);

        // Assert
        assertNull(result);

        verify(personRepository).findById(personId);
    }

    @Test
    void shouldCallRepositoryWithCorrectId() {
        // Arrange
        Long personId = 42L;
        Person expectedPerson = Person.builder()
                .id(42L)
                .identification("98765")
                .name("Jane Doe")
                .email("jane@example.com")
                .build();

        when(personRepository.findById(personId)).thenReturn(expectedPerson);

        // Act
        getPersonUseCase.execute(personId);

        // Assert
        verify(personRepository).findById(42L);
    }
}
