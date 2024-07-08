package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CreateProjectCommand {
    String name;
    String shortDescription;
    String longDescription;
    UUID firstProjectLeaderId;
    List<Long> githubUserIdsAsProjectLeadersToInvite;
    List<Long> githubRepoIds;
    List<NamedLink> moreInfos;
    Boolean isLookingForContributors;
    String imageUrl;
    List<UUID> ecosystemIds;
    List<UUID> categoryIds;
    List<String> categorySuggestions;
}
