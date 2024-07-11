package onlydust.com.marketplace.accounting.domain.service;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardUsdEquivalentStorage;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

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
    public void refreshRewardsUsdEquivalents() {
        rewardStatusStorage.notRequested()
                .stream().map(RewardStatusData::rewardId)
                .forEach(this::refreshRewardsUsdEquivalentOf);
    }

    @Override
    public void refreshRelatedRewardsStatuses(SponsorAccountStatement sponsorAccount) {
        final var accountBookFacade = sponsorAccount.accountBookFacade();

        sponsorAccount.awaitingPayments().forEach((rewardId, amount) -> rewardStatusStorage.updateAccountingData(rewardId,
                accountBookFacade.isFunded(rewardId),
                accountBookFacade.unlockDateOf(rewardId).map(d -> d.atZone(ZoneOffset.UTC)).orElse(null),
                accountBookFacade.networksOf(rewardId),
                usdAmountOf(rewardId).orElse(null)));
    }

    @Override
    public void refreshRewardsUsdEquivalentOf(BillingProfile.Id billingProfileId) {
        rewardStatusStorage.notRequested(billingProfileId)
                .stream().map(RewardStatusData::rewardId)
                .forEach(this::refreshRewardsUsdEquivalentOf);
    }

    @Override
    public void refreshRewardsUsdEquivalentOf(List<RewardId> rewardIds) {
        rewardIds.forEach(this::refreshRewardsUsdEquivalentOf);
    }

    @Override
    public void refreshRewardsUsdEquivalentOf(RewardId rewardId) {
        rewardStatusStorage.updateUsdAmount(rewardId, usdAmountOf(rewardId).orElse(null));
    }

    @Override
    public void create(AccountBookFacade accountBookFacade, RewardId rewardId) {
        rewardStatusStorage.persist(new RewardStatusData(rewardId).sponsorHasEnoughFund(accountBookFacade.isFunded(rewardId))
                .unlockDate(accountBookFacade.unlockDateOf(rewardId).map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .withAdditionalNetworks(accountBookFacade.networksOf(rewardId))
                .usdAmount(usdAmountOf(rewardId).orElse(null)));
    }

    @Override
    public void delete(RewardId rewardId) {
        rewardStatusStorage.delete(rewardId);
    }
}
