package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UpdateProjectCommand {
    UUID id;
    String name;
    String shortDescription;
    String longDescription;
    List<Long> githubUserIdsAsProjectLeadersToInvite;
    List<UUID> projectLeadersToKeep;
    List<Long> githubRepoIds;
    List<MoreInfoLink> moreInfos;
    Boolean isLookingForContributors;
    String imageUrl;
    ProjectRewardSettings rewardSettings;
}
