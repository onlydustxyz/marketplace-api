package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

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
    Boolean isInstalled;
}
