package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class BillingProfileRewardView {
    UUID id;
    ProjectId projectId;
    Integer numberOfRewardedContributions;
    Date requestedAt;
    Date processedAt;
    String rewardedOnProjectName;
    String rewardedOnProjectLogoUrl;
    RewardStatus status;
    Date unlockDate;
    MoneyView amount;
    Long recipientId;
    String recipientLogin;
    String recipientAvatarUrl;
    UUID billingProfileId;
}
