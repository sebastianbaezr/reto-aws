package co.com.bancolombia.config;

import co.com.bancolombia.model.person.gateways.PersonRepository;
import co.com.bancolombia.usecase.validator.PersonValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Bean
    public PersonValidator personValidator(PersonRepository personRepository) {
        return new PersonValidator(personRepository);
    }
}
