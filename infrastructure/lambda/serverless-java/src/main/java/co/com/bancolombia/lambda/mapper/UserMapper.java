package co.com.bancolombia.lambda.mapper;

import co.com.bancolombia.lambda.dto.UserRequestDto;
import co.com.bancolombia.lambda.dto.UserResponseDto;
import co.com.bancolombia.lambda.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User requestToModel(UserRequestDto request);

    UserResponseDto modelToResponse(User user);
}
