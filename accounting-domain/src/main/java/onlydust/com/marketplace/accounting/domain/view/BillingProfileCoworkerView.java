package onlydust.com.marketplace.accounting.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.net.URI;
import java.time.ZonedDateTime;

@Value
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class BillingProfileCoworkerView {
    UserId userId;
    GithubUserId githubUserId;
    String login;
    URI githubHtmlUrl;
    String avatarUrl;
    BillingProfile.User.Role role;
    ZonedDateTime joinedAt;
    ZonedDateTime invitedAt;

    @Getter(AccessLevel.NONE)
    Integer rewardCount;
    @Getter(AccessLevel.NONE)
    Integer billingProfileAdminCount;

    public boolean removable() {
        return !hasJoined() || (!isLastAdmin() && !hasInvoicedRewards());
    }

    private boolean hasInvoicedRewards() {
        return rewardCount != null && rewardCount > 0;
    }

    private boolean isLastAdmin() {
        return role == BillingProfile.User.Role.ADMIN && billingProfileAdminCount != null && billingProfileAdminCount <= 1;
    }

    public boolean hasJoined() {
        return joinedAt != null;
    }
}
