package co.com.bancolombia.usecase.person.getperson;

import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPersonUseCase {
    private final PersonRepository personRepository;

    public Person execute(Long id) {
        return personRepository.findById(id);
    }
}
