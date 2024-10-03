package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UpdateProjectCommand {
    ProjectId id;
    String name;
    String shortDescription;
    String longDescription;
    List<Long> githubUserIdsAsProjectLeadersToInvite;
    List<UserId> projectLeadersToKeep;
    List<Long> githubRepoIds;
    List<NamedLink> moreInfos;
    Boolean isLookingForContributors;
    String imageUrl;
    ProjectRewardSettings rewardSettings;
    List<UUID> ecosystemIds;
    List<UUID> categoryIds;
    List<String> categorySuggestions;
    List<ProjectContributorLabel> contributorLabels;
}
