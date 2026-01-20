package co.com.bancolombia.jpa.entity.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonJPARepository extends JpaRepository<PersonEntity, Long> {
    Optional<PersonEntity> findByIdentification(String identification);
    Optional<PersonEntity> findByEmail(String email);
}
