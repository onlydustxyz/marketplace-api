package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.NumberKpi;
import onlydust.com.marketplace.api.contract.model.ProgramProjectsPageItemResponse;
import onlydust.com.marketplace.api.contract.model.Trend;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper.map;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "project_stats", schema = "bi")
public class ProjectStatReadEntity {
    @Id
    @NonNull
    UUID projectId;

    @NonNull
    @OneToOne
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectReadEntity project;

    @OneToMany(mappedBy = "stats")
    @NonNull
    List<ProjectStatPerCurrencyReadEntity> statsPerCurrency;

    int currentPeriodMergedPrCount;
    int lastPeriodMergedPrCount;
    int currentPeriodActiveContributorCount;
    int lastPeriodActiveContributorCount;
    int currentPeriodNewContributorCount;
    int lastPeriodNewContributorCount;

    @NonNull
    BigDecimal averageRewardUsdAmount;

    public ProgramProjectsPageItemResponse toProgramProjectPageItemResponse() {
        final var totalGranted = map(statsPerCurrency, ProjectStatPerCurrencyReadEntity::totalGranted);
        final var totalRewarded = map(statsPerCurrency, ProjectStatPerCurrencyReadEntity::totalRewarded);

        return new ProgramProjectsPageItemResponse()
                .id(projectId)
                .slug(project.slug)
                .name(project.name)
                .logoUrl(project.logoUrl)
                .leads(project.leads.stream().map(AllUserReadEntity::toRegisteredUserResponse).toList())
                .totalAvailable(map(statsPerCurrency, s -> s.totalGranted().subtract(s.totalRewarded())))
                .totalGranted(totalGranted)
                .totalRewarded(totalRewarded)
                .percentUsedBudget(totalGranted.getTotalUsdEquivalent().compareTo(ZERO) == 0 ? null :
                        totalRewarded.getTotalUsdEquivalent().divide(totalGranted.getTotalUsdEquivalent(), 2, RoundingMode.HALF_EVEN))
                .averageRewardUsdAmount(prettyUsd(averageRewardUsdAmount))
                .mergedPrCount(createKpi(currentPeriodMergedPrCount, lastPeriodMergedPrCount))
                .newContributorsCount(createKpi(currentPeriodNewContributorCount, lastPeriodNewContributorCount))
                .activeContributorsCount(createKpi(currentPeriodActiveContributorCount, lastPeriodActiveContributorCount));
    }

    private NumberKpi createKpi(int current, int last) {
        return new NumberKpi()
                .value(current)
                .trend(current < last ? Trend.DOWN : current > last ? Trend.UP : Trend.STABLE);
    }
}
