package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.List;

@AllArgsConstructor
public class PayoutPreferenceService implements PayoutPreferenceFacadePort {

    private final PayoutPreferenceStoragePort payoutPreferenceStoragePort;
    private final BillingProfileStoragePort billingProfileStoragePort;

    @Override
    public List<PayoutPreferenceView> getPayoutPreferences(UserId userId) {
        return payoutPreferenceStoragePort.findAllByUserId(userId);
    }

    @Override
    public void setPayoutPreference(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId) {
        if (!billingProfileStoragePort.isUserMemberOf(billingProfileId, userId))
            throw OnlyDustException.forbidden("User %s is not member of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        if (!payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId))
            throw OnlyDustException.forbidden("Cannot set payout preference for user %s on project %s because user has not received any rewards on it"
                    .formatted(userId.value(), projectId.value()));
        payoutPreferenceStoragePort.save(projectId, billingProfileId, userId);
    }
}
