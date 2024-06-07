package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.project.domain.view.BudgetView;
import onlydust.com.marketplace.project.domain.view.ProjectBudgetsView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresProjectRewardAdapter implements ProjectRewardStoragePort {
    private final ProjectAllowanceRepository projectAllowanceRepository;
    private final HistoricalQuoteRepository historicalQuoteRepository;
    private final CurrencyRepository currencyRepository;
    private final RewardViewRepository rewardViewRepository;
    private final CustomRewardRepository customRewardRepository;

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
