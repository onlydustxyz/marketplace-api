package onlydust.com.marketplace.api.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateProjectCommand {

  String name;
  String shortDescription;
  String longDescription;
  UUID firstProjectLeaderId;
  List<Long> githubUserIdsAsProjectLeadersToInvite;
  List<Long> githubRepoIds;
  List<MoreInfoLink> moreInfos;
  Boolean isLookingForContributors;
  String imageUrl;
}
