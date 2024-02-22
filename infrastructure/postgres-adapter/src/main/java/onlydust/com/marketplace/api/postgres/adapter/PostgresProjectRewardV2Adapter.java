package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HistoricalQuoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectAllowanceRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.Currency;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.project.domain.view.*;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class PostgresProjectRewardV2Adapter implements ProjectRewardStoragePort {

    private final ProjectAllowanceRepository projectAllowanceRepository;
    private final HistoricalQuoteRepository historicalQuoteRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional
    public ProjectRewardsPageView findRewards(UUID projectId, ProjectRewardView.Filters filters, ProjectRewardView.SortBy sortBy, SortDirection sortDirection
            , int pageIndex, int pageSize) {
        // TODO
        return null;
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
                    .currency(Currency.valueOf(currency.code()))
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
    public RewardView getProjectReward(UUID rewardId) {
        // TODO
        return null;
    }

    @Override
    @Transactional
    public Page<RewardItemView> getProjectRewardItems(UUID rewardId, int pageIndex, int pageSize) {
        // TODO
        return null;
    }
}
