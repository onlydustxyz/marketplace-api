package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HistoricalQuoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectAllowanceRepository;

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
            final var currency = currencyRepository.findById(projectAllowanceEntity.getCurrencyId()).orElseThrow(() -> internalServerError("USD currency not " +
                                                                                                                                           "found"));
            final var quote = historicalQuoteRepository.findFirstByCurrencyIdAndBaseIdAndTimestampLessThanEqualOrderByTimestampDesc(
                            projectAllowanceEntity.getCurrencyId(),
                            usd.id(),
                            Instant.now()
                    ).map(HistoricalQuoteEntity::toDomain)
                    .orElseThrow(() -> internalServerError("No quote found for currency %s and base %s"
                            .formatted(projectAllowanceEntity.getCurrencyId(), usd.id())));
            return BudgetView.builder()
                    .currency(Currency.valueOf(currency.code()))
                    .dollarsConversionRate(quote.price())
                    .remaining(projectAllowanceEntity.getCurrentAllowance())
                    .initialAmount(projectAllowanceEntity.getInitialAllowance())
                    .remainingDollarsEquivalent(quote.convertToBaseCurrency(projectAllowanceEntity.getCurrentAllowance()))
                    .initialDollarsEquivalent(quote.convertToBaseCurrency(projectAllowanceEntity.getInitialAllowance()))
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
