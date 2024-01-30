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
@Value
@Table(name = "ledgers", schema = "sandbox")
@Builder(access = AccessLevel.PRIVATE)
public class LedgerEntity {
    @Id
    @NonNull UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @NonNull CurrencyEntity currency;

    @OneToMany(mappedBy = "ledgerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    List<SponsorLedgerEntity> sponsor = new ArrayList<>();

    @OneToMany(mappedBy = "ledgerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    List<ProjectLedgerEntity> project = new ArrayList<>();

    @OneToMany(mappedBy = "ledgerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    List<ContributorLedgerEntity> contributor = new ArrayList<>();

    @OneToMany(mappedBy = "ledgerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    List<LedgerTransactionEntity> transactions = new ArrayList<>();

    private Object owner() {
        if (!sponsor.isEmpty()) {
            return sponsor.get(0).getSponsorId();
        } else if (!project.isEmpty()) {
            return project.get(0).getProjectId();
        } else if (!contributor.isEmpty()) {
            return contributor.get(0).getGithubUserId();
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
        return LedgerEntity.builder()
                .id(ledger.id().value())
                .currency(CurrencyEntity.of(ledger.currency()))
                .sponsor(ledger.ownerId() instanceof SponsorId sponsorId ? List.of(SponsorLedgerEntity.of(ledger.id(), sponsorId)) : null)
                .project(ledger.ownerId() instanceof ProjectId projectId ? List.of(ProjectLedgerEntity.of(ledger.id(), projectId)) : null)
                .contributor(ledger.ownerId() instanceof ContributorId contributorId ? List.of(ContributorLedgerEntity.of(ledger.id(), contributorId)) : null)
                .transactions(ledger.getTransactions().stream().map(LedgerTransactionEntity::of).toList())
                .build();
    }
}

