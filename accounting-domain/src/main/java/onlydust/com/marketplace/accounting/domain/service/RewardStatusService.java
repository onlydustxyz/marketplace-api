package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static onlydust.com.marketplace.accounting.domain.model.Currency.Code.USD;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class RewardStatusService implements AccountingObserver, RewardStatusFacadePort {
    private final RewardStatusStorage rewardStatusStorage;
    private final RewardUsdEquivalentStorage rewardUsdEquivalentStorage;
    private final QuoteStorage quoteStorage;
    private final CurrencyStorage currencyStorage;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {
        refreshRelatedRewardsStatuses(sponsorAccount);
    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {
        refreshRelatedRewardsStatuses(sponsorAccount);
    }

    private void refreshRelatedRewardsStatuses(SponsorAccountStatement sponsorAccount) {
        sponsorAccount.awaitingPayments().forEach((rewardId, amount) -> {
            final var rewardStatus = rewardStatusStorage.get(rewardId)
                    .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(rewardId)));
            rewardStatusStorage.save(uptodateRewardStatus(sponsorAccount.accountBookFacade(), rewardStatus));
        });
    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        rewardStatusStorage.save(uptodateRewardStatus(accountBookFacade, new RewardStatus(rewardId)));
        updateUsdEquivalent(rewardId);
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
        rewardStatusStorage.delete(rewardId);
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        final var rewardStatus = rewardStatusStorage.get(rewardId)
                .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(rewardId)));
        rewardStatusStorage.save(rewardStatus.paidAt(ZonedDateTime.now()));
    }

    public void updateUsdEquivalent(RewardId rewardId) {
        final var rewardStatus = rewardStatusStorage.get(rewardId)
                .orElseThrow(() -> notFound("RewardStatus not found for reward %s".formatted(rewardId)));
        rewardStatusStorage.save(rewardStatus.amountUsdEquivalent(usdEquivalent(rewardId)));
    }

    @Override
    public BigDecimal usdEquivalent(RewardId rewardId) {
        final var usd = currencyStorage.findByCode(Currency.Code.of(USD))
                .orElseThrow(() -> internalServerError("Currency USD not found"));

        return rewardUsdEquivalentStorage.get(rewardId).flatMap(
                        rewardUsdEquivalent -> rewardUsdEquivalent.equivalenceSealingDate()
                                .flatMap(date -> quoteStorage.nearest(rewardUsdEquivalent.rewardCurrencyId(), usd.id(), date))
                                .map(quote -> quote.convertToBaseCurrency(rewardUsdEquivalent.rewardAmount())))
                .orElse(null);
    }

    private RewardStatus uptodateRewardStatus(AccountBookFacade accountBookFacade, RewardStatus rewardStatus) {
        return rewardStatus
                .sponsorHasEnoughFund(accountBookFacade.isFunded(rewardStatus.rewardId()))
                .unlockDate(accountBookFacade.unlockDateOf(rewardStatus.rewardId()).map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .withAdditionalNetworks(accountBookFacade.networksOf(rewardStatus.rewardId()))
                .amountUsdEquivalent(usdEquivalent(rewardStatus.rewardId()));
    }

    public void refreshRewardsUsdEquivalents() {
        rewardStatusStorage.notPaid().forEach(rewardStatus ->
                rewardStatusStorage.save(rewardStatus.amountUsdEquivalent(usdEquivalent(rewardStatus.rewardId())))
        );
    }
}
