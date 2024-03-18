package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;

import java.time.ZonedDateTime;

@Builder
public record BillingProfileUserRightsView(BillingProfile.User.Role role, Boolean hasBillingProfileSomeInvoices,
                                           Boolean hasUserSomeRewardsIncludedInInvoicesOnBillingProfile, InvitationView invitation) {
    @Builder
    public record InvitationView(@NonNull ZonedDateTime invitedAt, @NonNull BillingProfile.User.Role role, @NonNull GithubUserId githubUserId,
                                 @NonNull String githubLogin, @NonNull String githubAvatarUrl) {
    }

    public Boolean canDelete() {
        return !this.hasBillingProfileSomeInvoices && this.role == BillingProfile.User.Role.ADMIN;
    }

    public Boolean canLeave() {
        return !this.hasUserSomeRewardsIncludedInInvoicesOnBillingProfile && this.role == BillingProfile.User.Role.MEMBER;
    }
}
