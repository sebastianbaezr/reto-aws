package co.com.bancolombia.api.controller;

import co.com.bancolombia.api.dto.ApiResponse;
import co.com.bancolombia.api.dto.PersonRequest;
import co.com.bancolombia.api.dto.PersonResponse;
import co.com.bancolombia.api.mapper.PersonDtoMapper;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.usecase.person.createperson.CreatePersonUseCase;
import co.com.bancolombia.usecase.person.getperson.GetPersonUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    @Mock
    private CreatePersonUseCase createPersonUseCase;

    @Mock
    private GetPersonUseCase getPersonUseCase;

    @Mock
    private PersonDtoMapper personDtoMapper;

    private PersonController personController;

    @BeforeEach
    void setUp() {
        personController = new PersonController(createPersonUseCase, getPersonUseCase, personDtoMapper);
    }

    @Test
    void shouldCreatePersonSuccessfully() {
        // Arrange
        PersonRequest request = PersonRequest.builder()
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        Person domainPerson = Person.builder()
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        Person createdPerson = Person.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        PersonResponse response = PersonResponse.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(personDtoMapper.requestToDomain(request)).thenReturn(domainPerson);
        when(createPersonUseCase.execute(domainPerson)).thenReturn(createdPerson);
        when(personDtoMapper.domainToResponse(createdPerson)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse<PersonResponse>> result = personController.createPerson(request);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getData().getId());
        assertEquals("12345", result.getBody().getData().getIdentification());

        verify(personDtoMapper).requestToDomain(request);
        verify(createPersonUseCase).execute(domainPerson);
        verify(personDtoMapper).domainToResponse(createdPerson);
    }

    @Test
    void shouldGetPersonSuccessfully() {
        // Arrange
        Long personId = 1L;
        Person person = Person.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        PersonResponse response = PersonResponse.builder()
                .id(1L)
                .identification("12345")
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(getPersonUseCase.execute(personId)).thenReturn(person);
        when(personDtoMapper.domainToResponse(person)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse<PersonResponse>> result = personController.getPerson(personId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getData().getId());
        assertEquals("john@example.com", result.getBody().getData().getEmail());

        verify(getPersonUseCase).execute(personId);
        verify(personDtoMapper).domainToResponse(person);
    }
}
