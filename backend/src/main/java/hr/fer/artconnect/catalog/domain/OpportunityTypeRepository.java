package hr.fer.artconnect.catalog.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunityTypeRepository extends JpaRepository<OpportunityType, UUID> {
    List<OpportunityType> findAllByActiveTrueOrderByPositionAsc();
}
