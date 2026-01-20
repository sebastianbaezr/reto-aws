package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.PersonRequest;
import co.com.bancolombia.api.dto.PersonResponse;
import co.com.bancolombia.model.person.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonDtoMapper {

    @Mapping(target = "id", ignore = true)
    Person requestToDomain(PersonRequest request);

    PersonResponse domainToResponse(Person person);
}
