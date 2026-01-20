package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.PersonErrorCode;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PersonValidator {
    private final PersonRepository personRepository;

    public void validateIdentificationNotExists(String identification) {
        Person existingPerson = personRepository.findByIdentification(identification);
        if (existingPerson != null) {
            throw new BusinessException(PersonErrorCode.IDENTIFICATION_ALREADY_EXISTS);
        }
    }

    public void validateEmailNotExists(String email) {
        Person existingPerson = personRepository.findByEmail(email);
        if (existingPerson != null) {
            throw new BusinessException(PersonErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}
