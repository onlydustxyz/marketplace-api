package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.net.URI;
import java.time.ZonedDateTime;

@Value
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class BillingProfileCoworkerView {
    UserId userId;
    Long githubUserId;
    String login;
    URI githubHtmlUrl;
    String avatarUrl;
    BillingProfile.User.Role role;
    ZonedDateTime joinedAt;
    ZonedDateTime invitedAt;
    int rewardCount;

    Boolean removable;
}
