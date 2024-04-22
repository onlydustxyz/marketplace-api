package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetStatsEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardDetailsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.project.domain.view.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresProjectRewardAdapter implements ProjectRewardStoragePort {
    private final ProjectAllowanceRepository projectAllowanceRepository;
    private final HistoricalQuoteRepository historicalQuoteRepository;
    private final CurrencyRepository currencyRepository;
    private final BudgetStatsRepository budgetStatsRepository;
    private final RewardDetailsViewRepository rewardDetailsViewRepository;
    private final RewardViewRepository rewardViewRepository;
    private final CustomRewardRepository customRewardRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ProjectRewardsPageView findRewards(UUID projectId, ProjectRewardView.Filters filters,
                                              Reward.SortBy sort, SortDirection sortDirection,
                                              int pageIndex, int pageSize) {
        final var format = new SimpleDateFormat("yyyy-MM-dd");
        final var fromDate = isNull(filters.getFrom()) ? null : format.format(filters.getFrom());
        final var toDate = isNull(filters.getTo()) ? null : format.format(filters.getTo());

        final var pageRequest = PageRequest.of(pageIndex, pageSize,
                RewardDetailsViewRepository.sortBy(sort, sortDirection == SortDirection.asc ? Sort.Direction.ASC : Sort.Direction.DESC));

        final var page = rewardDetailsViewRepository.findProjectRewards(projectId, filters.getCurrencies(), filters.getContributors(), fromDate, toDate,
                pageRequest);
        final var budgetStats = budgetStatsRepository.findByProject(projectId, filters.getCurrencies(), filters.getContributors(), fromDate, toDate);

        return ProjectRewardsPageView.builder().
                rewards(Page.<ProjectRewardView>builder()
                        .content(page.getContent().stream().map(RewardDetailsViewEntity::toProjectReward).toList())
                        .totalItemNumber((int) page.getTotalElements())
                        .totalPageNumber(page.getTotalPages())
                        .build())
                .budgetStatsPerCurrency(budgetStats.stream()
                        .map(budgetStatsEntity -> new ProjectRewardsPageView.BudgetStats(
                                new Money(budgetStatsEntity.getRemainingAmount(),
                                        budgetStatsEntity.getCurrency().toView(),
                                        budgetStatsEntity.getRemainingUsdAmount()),
                                new Money(budgetStatsEntity.getSpentAmount(),
                                        budgetStatsEntity.getCurrency().toView(),
                                        budgetStatsEntity.getSpentUsdAmount())
                        ))
                        .toList())
                .sentRewardsCount(budgetStats.stream().map(BudgetStatsEntity::getRewardIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributionsCount(budgetStats.stream().map(BudgetStatsEntity::getRewardItemIds).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributorsCount(budgetStats.stream().map(BudgetStatsEntity::getRewardRecipientIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .build();
    }

    @Override
    @Transactional
    public ProjectBudgetsView findBudgets(UUID projectId) {
        final var usd = currencyRepository.findByCode("USD").orElseThrow(() -> internalServerError("USD currency not found"));

        final var budgets = projectAllowanceRepository.findAllByProjectId(projectId).stream().map(projectAllowanceEntity -> {
            final var currency = currencyRepository.findById(projectAllowanceEntity.getCurrencyId())
                    .orElseThrow(() -> internalServerError("Currency %s not found".formatted(projectAllowanceEntity.getCurrencyId())));

            final var quote = historicalQuoteRepository.findFirstByBaseIdAndTargetIdAndTimestampLessThanEqualOrderByTimestampDesc(
                    projectAllowanceEntity.getCurrencyId(),
                    usd.id(),
                    Instant.now()
            ).map(HistoricalQuoteEntity::toDomain);

            return BudgetView.builder()
                    .currency(currency.toView())
                    .dollarsConversionRate(quote.map(Quote::price).orElse(null))
                    .remaining(projectAllowanceEntity.getCurrentAllowance())
                    .initialAmount(projectAllowanceEntity.getInitialAllowance())
                    .remainingDollarsEquivalent(quote.map(q -> q.convertToBaseCurrency(projectAllowanceEntity.getCurrentAllowance())).orElse(null))
                    .initialDollarsEquivalent(quote.map(q -> q.convertToBaseCurrency(projectAllowanceEntity.getInitialAllowance())).orElse(null))
                    .build();
        }).toList();

        return ProjectBudgetsView.builder().budgets(budgets).build();
    }

    @Override
    @Transactional
    public RewardDetailsView getProjectReward(UUID rewardId) {
        return rewardViewRepository.findById(rewardId)
                .orElseThrow(() -> notFound("Reward %s not found".formatted(rewardId)))
                .toView();
    }

    @Override
    @Transactional
    public Page<RewardItemView> getProjectRewardItems(UUID rewardId, int pageIndex, int pageSize) {
        final Integer count = customRewardRepository.countRewardItemsForRewardId(rewardId);
        final List<RewardItemView> rewardItemViews =
                customRewardRepository.findRewardItemsByRewardId(rewardId, pageIndex, pageSize)
                        .stream()
                        .map(RewardMapper::itemToDomain)
                        .toList();
        return Page.<RewardItemView>builder()
                .content(rewardItemViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }
}
