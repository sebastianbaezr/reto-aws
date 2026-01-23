package co.com.bancolombia.lambda.repository;

import co.com.bancolombia.lambda.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

    private static final Map<String, User> users = new ConcurrentHashMap<>();

    static {
        users.put("1", User.builder()
                .id("1")
                .nombre("Juan Pérez")
                .email("juan.perez@bancolombia.com")
                .build());
        users.put("2", User.builder()
                .id("2")
                .nombre("María López")
                .email("maria.lopez@bancolombia.com")
                .build());
        users.put("3", User.builder()
                .id("3")
                .nombre("Carlos Rodríguez")
                .email("carlos.rodriguez@bancolombia.com")
                .build());
    }

    @Override
    public User create(User user) {
        String id = UUID.randomUUID().toString();
        User newUser = user.toBuilder().id(id).build();
        users.put(id, newUser);
        return newUser;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User update(String id, User user) {
        User updatedUser = user.toBuilder().id(id).build();
        users.put(id, updatedUser);
        return updatedUser;
    }

    @Override
    public void delete(String id) {
        users.remove(id);
    }

    @Override
    public boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean emailExistsExcept(String email, String excludeId) {
        return users.values().stream()
                .filter(user -> !user.getId().equals(excludeId))
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }
}
