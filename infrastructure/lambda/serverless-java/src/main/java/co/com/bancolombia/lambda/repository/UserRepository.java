package co.com.bancolombia.lambda.repository;

import co.com.bancolombia.lambda.model.User;
import java.util.Optional;

public interface UserRepository {

    User create(User user);

    Optional<User> findById(String id);

    User update(String id, User user);

    void delete(String id);

    boolean emailExists(String email);

    boolean emailExistsExcept(String email, String excludeId);
}
