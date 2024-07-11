package onlydust.com.marketplace.accounting.domain.service;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardUsdEquivalentStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Slf4j
public class RewardStatusService implements RewardStatusFacadePort {
    // TODO migrate rewards to accounting schema and merge all those storages as onetone dependencies of reward
    private final RewardStatusStorage rewardStatusStorage;
    private final RewardUsdEquivalentStorage rewardUsdEquivalentStorage;
    private final QuoteStorage quoteStorage;
    private final Currency usd;

    public RewardStatusService(RewardStatusStorage rewardStatusStorage, RewardUsdEquivalentStorage rewardUsdEquivalentStorage, QuoteStorage quoteStorage,
                               CurrencyStorage currencyStorage) {
        this.rewardStatusStorage = rewardStatusStorage;
        this.rewardUsdEquivalentStorage = rewardUsdEquivalentStorage;
        this.quoteStorage = quoteStorage;
        this.usd = currencyStorage.findByCode(Currency.Code.USD).orElseThrow(() -> internalServerError("Currency USD not found"));
    }

    private Optional<ConvertedAmount> usdAmountOf(RewardId rewardId) {
        return rewardUsdEquivalentStorage.get(rewardId).flatMap(rewardUsdEquivalent -> {
            final var date = rewardUsdEquivalent.equivalenceSealingDate().orElse(ZonedDateTime.now());
            return quoteStorage.nearest(rewardUsdEquivalent.rewardCurrencyId(), usd.id(), date)
                    .map(quote -> new ConvertedAmount(Amount.of(quote.convertToBaseCurrency(rewardUsdEquivalent.rewardAmount())), quote.price()));
        });
    }

    @Override
    @Transactional
    public void refreshRewardsUsdEquivalents() {
        rewardStatusStorage.notRequested()
                .forEach(this::refreshUsdAmount);
    }

    @Override
    @Transactional
    public void refreshRelatedRewardsStatuses(SponsorAccountStatement sponsorAccount) {
        sponsorAccount.awaitingPayments().forEach((rewardId, amount) -> {
            final var rewardStatus = rewardStatusStorage.get(rewardId)
                    .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(rewardId)));
            update(sponsorAccount.accountBookFacade(), rewardStatus);
        });
    }

    @Override
    @Transactional
    public void refreshRewardsUsdEquivalentOf(BillingProfile.Id billingProfileId) {
        rewardStatusStorage.notRequested(billingProfileId)
                .forEach(this::refreshUsdAmount);
    }

    private void refreshUsdAmount(RewardStatusData rewardStatus) {
        rewardStatusStorage.save(rewardStatus
                .usdAmount(usdAmountOf(rewardStatus.rewardId()).orElse(null))
        );
    }

    private void update(AccountBookFacade accountBookFacade, RewardStatusData rewardStatus) {
        rewardStatusStorage.save(rewardStatus
                .sponsorHasEnoughFund(accountBookFacade.isFunded(rewardStatus.rewardId()))
                .unlockDate(accountBookFacade.unlockDateOf(rewardStatus.rewardId()).map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .withAdditionalNetworks(accountBookFacade.networksOf(rewardStatus.rewardId()))
                .usdAmount(usdAmountOf(rewardStatus.rewardId()).orElse(null)));
    }

    @Override
    @Transactional
    public void refreshRewardsUsdEquivalentOf(List<RewardId> rewardIds) {
        final var statuses = rewardStatusStorage.get(rewardIds);
        if (statuses.size() != rewardIds.size())
            throw OnlyDustException.internalServerError("Some reward statuses were not found");

        statuses.forEach(this::refreshUsdAmount);
    }

    @Override
    @Transactional
    public void refreshRewardsUsdEquivalentOf(RewardId rewardId) {
        refreshRewardsUsdEquivalentOf(List.of(rewardId));
    }

    @Override
    public void create(AccountBookFacade accountBookFacade, RewardId rewardId) {
        update(accountBookFacade, new RewardStatusData(rewardId));
    }

    @Override
    public void delete(RewardId rewardId) {
        rewardStatusStorage.delete(rewardId);
    }
}
