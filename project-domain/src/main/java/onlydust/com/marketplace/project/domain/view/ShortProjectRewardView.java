package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.RewardId;

@Data
@Builder
public class ShortProjectRewardView {
    @NonNull
    RewardId rewardId;
    @NonNull
    String projectName;
    @NonNull
    Money money;
    @NonNull
    ContributorLinkView recipient;
}
