package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ShortProjectRewardView {
    @NonNull
    UUID rewardId;
    @NonNull
    String projectName;
    @NonNull
    Money money;
    @NonNull
    ContributorLinkView recipient;
}
