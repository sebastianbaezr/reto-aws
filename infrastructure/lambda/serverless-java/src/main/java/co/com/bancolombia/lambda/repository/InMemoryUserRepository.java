package co.com.bancolombia.lambda.repository;

import co.com.bancolombia.lambda.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private static final Map<Long, User> users = new ConcurrentHashMap<>();
    private static final AtomicLong nextId = new AtomicLong(4);

    static {
        users.put(1L, User.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan.perez@bancolombia.com")
                .build());
        users.put(2L, User.builder()
                .id(2L)
                .nombre("María López")
                .email("maria.lopez@bancolombia.com")
                .build());
        users.put(3L, User.builder()
                .id(3L)
                .nombre("Carlos Rodríguez")
                .email("carlos.rodriguez@bancolombia.com")
                .build());
    }

    @Override
    public User create(User user) {
        long id = nextId.getAndIncrement();
        User newUser = user.toBuilder().id(id).build();
        users.put(id, newUser);
        return newUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User update(Long id, User user) {
        User updatedUser = user.toBuilder().id(id).build();
        users.put(id, updatedUser);
        return updatedUser;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean emailExistsExcept(String email, Long excludeId) {
        return users.values().stream()
                .filter(user -> !user.getId().equals(excludeId))
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }
}
