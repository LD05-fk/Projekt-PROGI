package hr.fer.artconnect.common.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_settings")
public class SystemSetting {
    @Id
    private String key;

    private String value;

    private OffsetDateTime updatedAt;

    private UUID updatedBy;

    protected SystemSetting() {}

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }
}
