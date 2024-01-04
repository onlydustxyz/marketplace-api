package onlydust.com.marketplace.api.domain.view;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

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
