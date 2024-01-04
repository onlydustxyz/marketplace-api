package onlydust.com.marketplace.api.domain.view;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectOrganizationView {

  Long id;
  String login;
  String avatarUrl;
  String htmlUrl;
  String name;
  Long installationId;
  @Builder.Default
  Set<ProjectOrganizationRepoView> repos = new HashSet<>();
  @Builder.Default
  Boolean isInstalled = false;
}
