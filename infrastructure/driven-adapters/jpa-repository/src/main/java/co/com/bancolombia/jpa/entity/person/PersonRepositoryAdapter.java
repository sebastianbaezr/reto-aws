package co.com.bancolombia.jpa.entity.person;

import co.com.bancolombia.jpa.helper.AdapterOperations;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepositoryAdapter extends AdapterOperations<Person, PersonEntity, Long, PersonJPARepository>
        implements PersonRepository {

    public PersonRepositoryAdapter(PersonJPARepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Person.class));
    }

    @Override
    public Person save(Person person) {
        return toEntity(repository.save(toData(person)));
    }

    @Override
    public Person findByIdentification(String identification) {
        return repository.findByIdentification(identification)
                .map(this::toEntity)
                .orElse(null);
    }

    @Override
    public Person findByEmail(String email) {
        return repository.findByEmail(email)
                .map(this::toEntity)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
