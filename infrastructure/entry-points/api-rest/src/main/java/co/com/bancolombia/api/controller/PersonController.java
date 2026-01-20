package co.com.bancolombia.api.controller;

import co.com.bancolombia.api.dto.ApiResponse;
import co.com.bancolombia.api.dto.PersonRequest;
import co.com.bancolombia.api.dto.PersonResponse;
import co.com.bancolombia.api.mapper.PersonDtoMapper;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.usecase.person.createperson.CreatePersonUseCase;
import co.com.bancolombia.usecase.person.getperson.GetPersonUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/people")
@RequiredArgsConstructor
public class PersonController {
    private final CreatePersonUseCase createPersonUseCase;
    private final GetPersonUseCase getPersonUseCase;
    private final PersonDtoMapper personDtoMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<PersonResponse>> createPerson(@Valid @RequestBody PersonRequest request) {
        Person person = personDtoMapper.requestToDomain(request);
        Person personCreated = createPersonUseCase.execute(person);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PersonResponse>builder()
                    .data(personDtoMapper.domainToResponse(personCreated))
                    .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonResponse>> getPerson(@PathVariable("id") Long id) {
        Person person = getPersonUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<PersonResponse>builder()
                .data(personDtoMapper.domainToResponse(person))
                .build());
    }
}
