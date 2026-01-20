package co.com.bancolombia.model.person.gateways;

import co.com.bancolombia.model.person.Person;

public interface PersonRepository {
    Person save(Person person);
    Person findById(Long id);
    Person findByIdentification(String identification);
    Person findByEmail(String email);
    Iterable<Person> findAll();
    void delete(Long id);
}
