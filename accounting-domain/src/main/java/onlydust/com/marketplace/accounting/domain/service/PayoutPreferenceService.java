package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@AllArgsConstructor
public class PayoutPreferenceService implements PayoutPreferenceFacadePort {

    private final PayoutPreferenceStoragePort payoutPreferenceStoragePort;
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final AccountingObserverPort accountingObserverPort;

    @Override
    public void setPayoutPreference(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId) {
        final var billingProfile = billingProfileStoragePort.findById(billingProfileId)
                .orElseThrow(() -> forbidden("Billing profile %s not found".formatted(billingProfileId.value())));

        if (!billingProfile.isMember(userId))
            throw forbidden("User %s is not member of billing profile %s".formatted(userId.value(), billingProfileId.value()));

        if (!payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId))
            throw forbidden("Cannot set payout preference for user %s on project %s because user has not received any rewards on it"
                    .formatted(userId.value(), projectId.value()));

        if (!billingProfile.enabled())
            throw forbidden("Cannot set payout preference for user %s on project %s because billing profile %s is disabled"
                    .formatted(userId, projectId, billingProfileId));

        payoutPreferenceStoragePort.save(projectId, billingProfileId, userId);
        accountingObserverPort.onPayoutPreferenceChanged(billingProfileId, userId, projectId);
    }
}
