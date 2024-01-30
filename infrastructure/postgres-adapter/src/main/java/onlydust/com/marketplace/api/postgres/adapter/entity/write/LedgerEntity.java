package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.ContributorId;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Table(name = "ledgers", schema = "sandbox")
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
    ContributorLedgerEntity contributor;

    @OneToMany(mappedBy = "ledgerId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    List<LedgerTransactionEntity> transactions = new ArrayList<>();

    private Object owner() {
        if (sponsor != null) {
            return sponsor.getSponsorId();
        } else if (project != null) {
            return project.getProjectId();
        } else if (contributor != null) {
            return contributor.getGithubUserId();
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
                .transactions(ledger.getTransactions().stream().map(LedgerTransactionEntity::of).toList())
                .build();

        if (ledger.ownerId() instanceof SponsorId sponsorId) {
            entity.setSponsor(SponsorLedgerEntity.of(entity, sponsorId));
        } else if (ledger.ownerId() instanceof ProjectId projectId) {
            entity.setProject(ProjectLedgerEntity.of(entity, projectId));
        } else if (ledger.ownerId() instanceof ContributorId contributorId) {
            entity.setContributor(ContributorLedgerEntity.of(entity, contributorId));
        } else {
            throw new IllegalStateException("Ledger must have an owner");
        }

        return entity;
    }
}

