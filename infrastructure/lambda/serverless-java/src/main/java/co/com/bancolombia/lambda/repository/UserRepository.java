package co.com.bancolombia.lambda.repository;

import co.com.bancolombia.lambda.model.User;
import java.util.Optional;

public interface UserRepository {

    User create(User user);

    Optional<User> findById(Long id);

    User update(Long id, User user);

    void delete(Long id);

    boolean emailExists(String email);

    boolean emailExistsExcept(String email, Long excludeId);
}
