package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
public class ProjectOrganizationRepoView {
    Long githubRepoId;
    String owner;
    String name;
    String description;
    Long starCount;
    Long forkCount;
    String url;
    Boolean hasIssues;
    Boolean isIncludedInProject;
    Boolean isAuthorizedInGithubApp;
    Map<String, Long> technologies;
    ZonedDateTime indexedAt;
}
