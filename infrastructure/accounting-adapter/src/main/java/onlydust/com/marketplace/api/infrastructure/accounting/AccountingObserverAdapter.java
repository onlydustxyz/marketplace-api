package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.port.input.AccountingRewardObserverPort;
import onlydust.com.marketplace.api.domain.port.input.AccountingUserObserverPort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.UUID;

@AllArgsConstructor
public class AccountingObserverAdapter implements AccountingUserObserverPort, AccountingRewardObserverPort {
    private final RewardStatusStorage rewardStatusStorage;
    private final CurrencyFacadePort currencyFacadePort;

    @Override
    public void onRewardCreated(Reward reward) {
        final var currency = currencyFacadePort.listCurrencies().stream()
                .filter(c -> c.code().toString().equals(reward.currency().toString().toUpperCase()))
                .findFirst()
                .orElseThrow(() -> OnlyDustException.badRequest("Unsupported currency %s".formatted(reward.currency())));

        final var rewardStatus = new RewardStatus(RewardId.of(reward.id())).rewardCurrency(currency);
        rewardStatusStorage.save(rewardStatus);
    }

    @Override
    public void onRewardCancelled(UUID rewardId) {
        rewardStatusStorage.delete(RewardId.of(rewardId));
    }

    @Override
    public void onPaymentRequested(UUID rewardId) {
        final var rewardStatus = rewardStatusStorage.get(RewardId.of(rewardId))
                .orElseThrow(() -> OnlyDustException.notFound("Reward status not found for reward %s".formatted(rewardId)));
        rewardStatusStorage.save(rewardStatus.paymentRequested(true));
    }

    @Override
    public void onInvoiceApproved(UUID rewardId) {

    }

    @Override
    public void onInvoiceRejected(UUID rewardId) {

    }

    @Override
    public void onBillingProfileUpdated(BillingProfileUpdated event) {

    }

    @Override
    public void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutInformation.PayoutSettings payoutSettings) {

    }

    @Override
    public void onBillingProfileSelected(UUID projectId, User user, IndividualBillingProfile billingProfile) {

    }

    @Override
    public void onBillingProfileSelected(UUID projectId, User user, CompanyBillingProfile billingProfile) {

    }
}
