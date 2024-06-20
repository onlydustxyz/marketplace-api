package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import onlydust.com.marketplace.project.domain.view.ProjectLedView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@ToString
public class User {
    UUID id;
    @Builder.Default
    List<AuthenticatedUser.Role> roles = new ArrayList<>();
    Long githubUserId;
    String githubAvatarUrl;
    String githubLogin;
    String githubEmail;
    @Builder.Default
    List<ProjectLedView> projectsLed = new ArrayList<>();
    @Builder.Default
    List<BillingProfileLinkView> billingProfiles = new ArrayList<>();

    public List<UUID> getAdministratedBillingProfiles() {
        return this.billingProfiles.stream()
                .filter(bp -> bp.role() == BillingProfileLinkView.Role.ADMIN)
                .map(BillingProfileLinkView::id)
                .toList();
    }

    public AuthenticatedUser asAuthenticatedUser() {
        return AuthenticatedUser.builder()
                .id(id)
                .roles(roles)
                .githubUserId(githubUserId)
                .projectsLed(projectsLed.stream().map(ProjectLedView::getId).toList())
                .administratedBillingProfiles(getAdministratedBillingProfiles())
                .build();
    }

    public GithubUserIdentity toGithubIdentity() {
        return GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .githubLogin(githubLogin)
                .githubAvatarUrl(githubAvatarUrl)
                .build();
    }
}
