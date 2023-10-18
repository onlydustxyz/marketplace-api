package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectLeaderLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.RegisteredUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;

public interface UserMapper {

    static ContributorLinkView mapToContributorLinkView(GithubUserViewEntity user) {
        return ContributorLinkView.builder()
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .url(user.getHtmlUrl())
                .build();
    }

    static ProjectLeaderLinkView mapToProjectLeaderLinkView(RegisteredUserViewEntity user) {
        return ProjectLeaderLinkView.builder()
                .id(user.getId())
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .url(user.getHtmlUrl())
                .build();
    }

    static User mapUserToDomain(UserViewEntity user, Date termsAndConditionsLatestVersionDate) {
        return User.builder()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .login(user.getGithubLogin())
                .avatarUrl(user.getGithubAvatarUrl())
                .roles(Arrays.stream(user.getRoles()).toList())
                .hasAcceptedLatestTermsAndConditions(nonNull(user.getOnboarding())
                                                     && nonNull(user.getOnboarding().getTermsAndConditionsAcceptanceDate())
                                                     && user.getOnboarding().getTermsAndConditionsAcceptanceDate().after(termsAndConditionsLatestVersionDate))
                .hasSeenOnboardingWizard(nonNull(user.getOnboarding())
                                         && nonNull(user.getOnboarding().getProfileWizardDisplayDate()))
                .build();
    }

    static User mapUserToDomain(RegisteredUserViewEntity user, Date termsAndConditionsLatestVersionDate) {
        return User.builder()
                .id(user.getId())
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .roles(Boolean.TRUE.equals(user.getAdmin()) ? List.of(UserRole.USER, UserRole.ADMIN) : List.of(UserRole.USER))
                .hasAcceptedLatestTermsAndConditions(nonNull(user.getOnboarding())
                                                     && nonNull(user.getOnboarding().getTermsAndConditionsAcceptanceDate())
                                                     && user.getOnboarding().getTermsAndConditionsAcceptanceDate().after(termsAndConditionsLatestVersionDate))
                .hasSeenOnboardingWizard(nonNull(user.getOnboarding())
                                         && nonNull(user.getOnboarding().getProfileWizardDisplayDate()))
                .build();
    }

    static UserEntity mapUserToEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .githubLogin(user.getLogin())
                .githubAvatarUrl(user.getAvatarUrl())
                .roles(user.getRoles() != null ? user.getRoles().toArray(UserRole[]::new) : new UserRole[0])
                .build();
    }
}
