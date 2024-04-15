package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import onlydust.com.marketplace.project.domain.view.ProjectLedView;

import java.util.ArrayList;
import java.util.Date;
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
    List<ProjectLedView> pendingProjectsLed = new ArrayList<>();
    @Builder.Default
    List<UUID> projectsAppliedTo = new ArrayList<>();
    @Builder.Default
    List<BillingProfileLinkView> billingProfiles = new ArrayList<>();
    boolean isMissingPayoutPreference;
    @Builder.Default
    List<Sponsor> sponsors = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    boolean hasAcceptedLatestTermsAndConditions;
    @Getter(AccessLevel.NONE)
    boolean hasSeenOnboardingWizard;
    Date createdAt;
    String firstName;
    String lastName;

    public boolean hasAcceptedLatestTermsAndConditions() {
        return hasAcceptedLatestTermsAndConditions;
    }

    public boolean hasSeenOnboardingWizard() {
        return hasSeenOnboardingWizard;
    }

    public boolean hasRole(AuthenticatedUser.Role role) {
        return roles.contains(role);
    }

    public List<BillingProfileLinkView> getAdministratedBillingProfile() {
        return this.billingProfiles.stream()
                .filter(bp -> bp.role() == BillingProfileLinkView.Role.ADMIN)
                .toList();
    }

    public AuthenticatedUser asAuthenticatedUser() {
        return AuthenticatedUser.builder()
                .id(id)
                .roles(roles)
                .githubUserId(githubUserId)
                .projectsLed(projectsLed.stream().map(ProjectLedView::getId).toList())
                .administratedBillingProfiles(getAdministratedBillingProfile().stream().map(BillingProfileLinkView::id).toList())
                .build();
    }
}
