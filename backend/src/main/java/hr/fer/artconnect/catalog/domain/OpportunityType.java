package hr.fer.artconnect.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.UUID;

@Entity
@Table(name = "opportunity_types")
public class OpportunityType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String code;

    private String name;

    private boolean active;

    private short position;

    protected OpportunityType() {}

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public short getPosition() {
        return position;
    }
}
