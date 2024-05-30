package onlydust.com.marketplace.bff.read.entities.committee;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.CommitteeProjectAllocationLinkResponse;
import onlydust.com.backoffice.api.contract.model.MoneyResponse;
import onlydust.com.backoffice.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.bff.read.entities.ShortCurrencyResponseEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.bff.read.mapper.CommitteeMapper.roundScore;

@Entity
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@IdClass(CommitteeBudgetAllocationReadEntity.PrimaryKey.class)
@Table(name = "committee_budget_allocations", schema = "public")
public class CommitteeBudgetAllocationReadEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID committeeId;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID currencyId;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID projectId;

    @ManyToOne
    @JoinColumn(name = "committeeId", insertable = false, updatable = false)
    private @NonNull CommitteeReadEntity committee;

    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    private @NonNull ShortCurrencyResponseEntity currency;

    @ManyToOne
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    private @NonNull ProjectViewEntity project;

    private @NonNull BigDecimal amount;

    @OneToMany
    @JoinColumns({
            @JoinColumn(name = "committeeId", referencedColumnName = "committeeId"),
            @JoinColumn(name = "projectId", referencedColumnName = "projectId")
    })
    Set<CommitteeJuryVoteReadEntity> votes;

    public CommitteeProjectAllocationLinkResponse toDto() {
        return new CommitteeProjectAllocationLinkResponse()
                .project(new ProjectLinkResponse()
                        .id(projectId)
                        .name(project.getName())
                        .slug(project.getSlug())
                        .logoUrl(project.getLogoUrl()))
                .allocation(new MoneyResponse()
                        .amount(amount)
                        .currency(currency.toDto()))
                .score(roundScore(votes.stream().filter(v -> v.score() != null).mapToInt(CommitteeJuryVoteReadEntity::score).average()));
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        private @NonNull UUID committeeId;
        private @NonNull UUID currencyId;
        private @NonNull UUID projectId;
    }
}
