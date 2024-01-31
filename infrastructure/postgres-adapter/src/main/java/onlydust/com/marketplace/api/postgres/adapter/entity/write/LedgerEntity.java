package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Table(name = "ledgers", schema = "accounting")
@Builder(access = AccessLevel.PRIVATE)
public class LedgerEntity {
    @Id
    @NonNull UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @NonNull CurrencyEntity currency;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "ledger")
    SponsorLedgerEntity sponsor;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "ledger")
    ProjectLedgerEntity project;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "ledger")
    RewardLedgerEntity reward;

    @OneToMany(mappedBy = "ledgerId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    List<LedgerTransactionEntity> transactions = new ArrayList<>();

    private Object owner() {
        if (sponsor != null) {
            return SponsorId.of(sponsor.getSponsorId());
        } else if (project != null) {
            return ProjectId.of(project.getProjectId());
        } else if (reward != null) {
            return RewardId.of(reward.getRewardId());
        } else {
            throw new IllegalStateException("Ledger must have an owner");
        }
    }

    public Ledger toLedger() {
        final var ledger = new Ledger(Ledger.Id.of(id), owner(), currency.toDomain());
        ledger.getTransactions().addAll(transactions.stream().map(LedgerTransactionEntity::toTransaction).toList());
        return ledger;
    }

    public static LedgerEntity of(Ledger ledger) {
        final var entity = LedgerEntity.builder()
                .id(ledger.id().value())
                .currency(CurrencyEntity.of(ledger.currency()))
                .transactions(ledger.getTransactions().stream().map(t -> LedgerTransactionEntity.of(ledger.id(), t)).toList())
                .build();

        if (ledger.ownerId() instanceof SponsorId sponsorId) {
            entity.setSponsor(SponsorLedgerEntity.of(entity, sponsorId));
        } else if (ledger.ownerId() instanceof ProjectId projectId) {
            entity.setProject(ProjectLedgerEntity.of(entity, projectId));
        } else if (ledger.ownerId() instanceof RewardId rewardId) {
            entity.setReward(RewardLedgerEntity.of(entity, rewardId));
        } else {
            throw new IllegalStateException("Ledger must have an owner (%s)".formatted(ledger.ownerId().getClass()));
        }

        return entity;
    }
}

