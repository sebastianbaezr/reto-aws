package co.com.bancolombia.lambda.repository;

import co.com.bancolombia.lambda.dto.UserDto;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository {
    private static final Map<Long, UserDto> users = new ConcurrentHashMap<>();
    private static final AtomicLong nextId = new AtomicLong(4);

    static {
        // Hardcoded initial users
        users.put(1L, UserDto.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan.perez@bancolombia.com")
                .build());
        users.put(2L, UserDto.builder()
                .id(2L)
                .nombre("María López")
                .email("maria.lopez@bancolombia.com")
                .build());
        users.put(3L, UserDto.builder()
                .id(3L)
                .nombre("Carlos Rodríguez")
                .email("carlos.rodriguez@bancolombia.com")
                .build());
    }

    public UserDto create(UserDto user) {
        long id = nextId.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    public Optional<UserDto> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public UserDto update(Long id, UserDto user) {
        user.setId(id);
        users.put(id, user);
        return user;
    }

    public void delete(Long id) {
        users.remove(id);
    }

    public boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public boolean emailExistsExcept(String email, Long id) {
        return users.values().stream()
                .filter(user -> !user.getId().equals(id))
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }
}
