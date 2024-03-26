package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;

import java.util.List;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@AllArgsConstructor
public class PayoutPreferenceService implements PayoutPreferenceFacadePort {

    private final PayoutPreferenceStoragePort payoutPreferenceStoragePort;
    private final BillingProfileStoragePort billingProfileStoragePort;
    private final AccountingObserverPort accountingObserverPort;

    @Override
    public List<PayoutPreferenceView> getPayoutPreferences(UserId userId) {
        return payoutPreferenceStoragePort.findAllByUserId(userId)
                .stream()
                .map(view -> {
                    if (nonNull(view.billingProfileView()) && !view.billingProfileView().enabled()) {
                        return view.toBuilder().billingProfileView(null).build();
                    }
                    return view;
                })
                .toList();
    }

    @Override
    public void setPayoutPreference(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId) {
        if (!billingProfileStoragePort.isUserMemberOf(billingProfileId, userId))
            throw forbidden("User %s is not member of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        if (!payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId))
            throw forbidden("Cannot set payout preference for user %s on project %s because user has not received any rewards on it"
                    .formatted(userId.value(), projectId.value()));
        if (!billingProfileStoragePort.isEnabled(billingProfileId))
            throw forbidden("Cannot set payout preference for user %s on project %s because billing profile %s is disabled"
                    .formatted(userId, projectId, billingProfileId));
        payoutPreferenceStoragePort.save(projectId, billingProfileId, userId);
        accountingObserverPort.onPayoutPreferenceChanged(billingProfileId, userId, projectId);
    }
}
