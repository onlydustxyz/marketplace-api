package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "committee_budget_allocations", schema = "public")
@IdClass(CommitteeBudgetAllocationEntity.PrimaryKey.class)
public class CommitteeBudgetAllocationEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID committeeId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID projectId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID currencyId;

    BigDecimal amount;

    public static CommitteeBudgetAllocationEntity fromDomain(Committee.Id committeeId, UUID currencyId, UUID projectId, BigDecimal amount) {
        return CommitteeBudgetAllocationEntity.builder()
                .committeeId(committeeId.value())
                .currencyId(currencyId)
                .projectId(projectId)
                .amount(amount)
                .build();
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID committeeId;
        UUID projectId;
        UUID currencyId;
    }
}
