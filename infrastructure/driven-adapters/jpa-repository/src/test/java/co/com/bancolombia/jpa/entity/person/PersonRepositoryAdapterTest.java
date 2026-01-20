package co.com.bancolombia.jpa.entity.person;

import co.com.bancolombia.model.person.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonRepositoryAdapterTest {

    @Mock
    private PersonJPARepository personJPARepository;

    @Mock
    private ObjectMapper objectMapper;

    private PersonRepositoryAdapter personRepositoryAdapter;

    @BeforeEach
    void setUp() {
        personRepositoryAdapter = new PersonRepositoryAdapter(personJPARepository, objectMapper);
    }

    @Test
    void shouldSavePersonSuccessfully() {
        // Arrange
        Person personToSave = Person.builder()
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        PersonEntity personEntity = PersonEntity.builder()
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        PersonEntity savedEntity = PersonEntity.builder()
                .id(1L)
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

        when(objectMapper.map(personToSave, PersonEntity.class)).thenReturn(personEntity);
        when(personJPARepository.save(personEntity)).thenReturn(savedEntity);
        when(objectMapper.map(savedEntity, Person.class)).thenReturn(savedPerson);

        // Act
        Person result = personRepositoryAdapter.save(personToSave);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345", result.getIdentification());

        verify(personJPARepository).save(any(PersonEntity.class));
    }

    @Test
    void shouldFindPersonByIdentificationSuccessfully() {
        // Arrange
        String identification = "12345";
        PersonEntity personEntity = PersonEntity.builder()
                .id(1L)
                .identification(identification)
                .name("John Doe")
                .email("john@example.com")
                .build();

        Person expectedPerson = Person.builder()
                .id(1L)
                .identification(identification)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(personJPARepository.findByIdentification(identification)).thenReturn(Optional.of(personEntity));
        when(objectMapper.map(personEntity, Person.class)).thenReturn(expectedPerson);

        // Act
        Person result = personRepositoryAdapter.findByIdentification(identification);

        // Assert
        assertNotNull(result);
        assertEquals(identification, result.getIdentification());

        verify(personJPARepository).findByIdentification(identification);
    }

    @Test
    void shouldReturnNullWhenIdentificationNotFound() {
        // Arrange
        String identification = "99999";
        when(personJPARepository.findByIdentification(identification)).thenReturn(Optional.empty());

        // Act
        Person result = personRepositoryAdapter.findByIdentification(identification);

        // Assert
        assertNull(result);

        verify(personJPARepository).findByIdentification(identification);
    }

    @Test
    void shouldFindPersonByEmailSuccessfully() {
        // Arrange
        String email = "john@example.com";
        PersonEntity personEntity = PersonEntity.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email(email)
                .build();

        Person expectedPerson = Person.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email(email)
                .build();

        when(personJPARepository.findByEmail(email)).thenReturn(Optional.of(personEntity));
        when(objectMapper.map(personEntity, Person.class)).thenReturn(expectedPerson);

        // Act
        Person result = personRepositoryAdapter.findByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());

        verify(personJPARepository).findByEmail(email);
    }

    @Test
    void shouldReturnNullWhenEmailNotFound() {
        // Arrange
        String email = "notfound@example.com";
        when(personJPARepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Person result = personRepositoryAdapter.findByEmail(email);

        // Assert
        assertNull(result);

        verify(personJPARepository).findByEmail(email);
    }
}
