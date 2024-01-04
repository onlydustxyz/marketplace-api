package onlydust.com.marketplace.api.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

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
