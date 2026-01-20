package co.com.bancolombia.usecase.person.createperson;

import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import co.com.bancolombia.usecase.validator.PersonValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePersonUseCase {
    private final PersonRepository personRepository;
    private final PersonValidator personValidator;

    public Person execute(Person person) {
        personValidator.validateIdentificationNotExists(person.getIdentification());
        personValidator.validateEmailNotExists(person.getEmail());

        person.setId(null);
        return personRepository.save(person);
    }
}
