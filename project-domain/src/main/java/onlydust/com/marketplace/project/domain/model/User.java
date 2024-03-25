package onlydust.com.marketplace.project.domain.model;

import lombok.*;
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
    List<UserRole> roles = new ArrayList<>();
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

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    public List<BillingProfileLinkView> getAdministratedBillingProfile() {
        return this.billingProfiles.stream()
                .filter(bp -> bp.role() == BillingProfileLinkView.Role.ADMIN)
                .toList();
    }
}
