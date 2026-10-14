package hr.fer.artconnect.catalog.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtFieldRepository extends JpaRepository<ArtField, UUID> {
    List<ArtField> findAllByActiveTrueOrderByPositionAsc();
}
