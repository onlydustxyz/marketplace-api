package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Value
@Table(name = "project_allowances", schema = "public")
@IdClass(ProjectAllowanceEntity.PrimaryKey.class)
public class ProjectAllowanceEntity {
    @Id
    @NonNull
    UUID projectId;
    @Id
    @NonNull
    UUID currencyId;
    @NonNull
    BigDecimal currentAllowance;
    @NonNull
    BigDecimal initialAllowance;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID currencyId;
    }
}
