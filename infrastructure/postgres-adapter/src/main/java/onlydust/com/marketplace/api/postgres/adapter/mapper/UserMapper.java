package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectLeaderLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectLedView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ContactInformationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserProfileInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactChanelEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactInformationIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface UserMapper {

    static ContributorLinkView mapToContributorLinkView(ContributorViewEntity user) {
        return ContributorLinkView.builder()
                .githubUserId(user.getGithubUserId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .url(user.getHtmlUrl())
                .isRegistered(user.getIsRegistered())
                .build();
    }

    static ProjectLeaderLinkView mapToProjectLeaderLinkView(ProjectLeadViewEntity user) {
        return ProjectLeaderLinkView.builder()
                .id(user.getId())
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .url(user.getHtmlUrl())
                .build();
    }

    static User mapUserToDomain(UserViewEntity user, Date termsAndConditionsLatestVersionDate,
                                List<ProjectLedIdViewEntity> projectLedIdViewEntities,
                                List<ApplicationEntity> applications) {
        return User.builder()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .login(user.getGithubLogin())
                .avatarUrl(user.getProfile() != null && user.getProfile().getAvatarUrl() != null ?
                        user.getProfile().getAvatarUrl() : user.getGithubAvatarUrl())
                .roles(Arrays.stream(user.getRoles()).toList())
                .hasAcceptedLatestTermsAndConditions(nonNull(user.getOnboarding())
                                                     && nonNull(user.getOnboarding().getTermsAndConditionsAcceptanceDate())
                                                     && user.getOnboarding().getTermsAndConditionsAcceptanceDate().after(termsAndConditionsLatestVersionDate))
                .hasSeenOnboardingWizard(nonNull(user.getOnboarding())
                                         && nonNull(user.getOnboarding().getProfileWizardDisplayDate()))
                .projectsLed(projectLedIdViewEntities.stream()
                        .filter(projectLedIdViewEntity -> !projectLedIdViewEntity.getPending())
                        .map(projectLedIdViewEntity -> ProjectLedView.builder()
                                .name(projectLedIdViewEntity.getName())
                                .logoUrl(projectLedIdViewEntity.getLogoUrl())
                                .slug(projectLedIdViewEntity.getProjectSlug())
                                .id(projectLedIdViewEntity.getId().getProjectId())
                                .contributorCount(projectLedIdViewEntity.getContributorCount())
                                .build()).toList())
                .pendingProjectsLed(projectLedIdViewEntities.stream()
                        .filter(ProjectLedIdViewEntity::getPending)
                        .map(projectLedIdViewEntity -> ProjectLedView.builder()
                                .name(projectLedIdViewEntity.getName())
                                .logoUrl(projectLedIdViewEntity.getLogoUrl())
                                .slug(projectLedIdViewEntity.getProjectSlug())
                                .id(projectLedIdViewEntity.getId().getProjectId())
                                .contributorCount(projectLedIdViewEntity.getContributorCount())
                                .build()).toList())
                .projectsAppliedTo(applications.stream().map(ApplicationEntity::getProjectId).toList())
                .build();
    }

    static UserEntity mapUserToEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .githubLogin(user.getLogin())
                .githubAvatarUrl(user.getAvatarUrl())
                .email("TODO") //TODO: get email from Auth0 JWT claims
                .roles(user.getRoles() != null ? user.getRoles().toArray(UserRole[]::new) : new UserRole[0])
                .lastSeenAt(new Date())
                .build();
    }

    static UserProfileInfoEntity mapUserProfileToEntity(UUID userId, UserProfile userProfile) {
        return UserProfileInfoEntity.builder()
                .avatarUrl(userProfile.getAvatarUrl())
                .bio(userProfile.getBio())
                .location(userProfile.getLocation())
                .website(userProfile.getWebsite())
                .cover(mapCoverToEntity(userProfile.getCover()))
                .contactInformations(mapContactInformationsToEntity(userId, userProfile.getContacts()))
                .languages(userProfile.getTechnologies())
                .allocatedTime(mapAllocatedTimeToEntity(userProfile.getAllocatedTimeToContribute()))
                .isLookingForAJob(userProfile.getIsLookingForAJob())
                .id(userId)
                .build();
    }

    static AllocatedTimeEnumEntity mapAllocatedTimeToEntity(UserAllocatedTimeToContribute allocatedTimeToContribute) {
        return isNull(allocatedTimeToContribute) ? null : switch (allocatedTimeToContribute) {
            case NONE -> AllocatedTimeEnumEntity.none;
            case LESS_THAN_ONE_DAY -> AllocatedTimeEnumEntity.less_than_one_day;
            case ONE_TO_THREE_DAYS -> AllocatedTimeEnumEntity.one_to_three_days;
            case GREATER_THAN_THREE_DAYS -> AllocatedTimeEnumEntity.greater_than_three_days;
        };
    }

    static List<ContactInformationEntity> mapContactInformationsToEntity(UUID userId, List<Contact> contacts) {
        return contacts.stream().map(contact -> ContactInformationEntity.builder()
                .id(ContactInformationIdEntity.builder()
                        .userId(userId)
                        .channel(switch (contact.getChannel()) {
                            case EMAIL -> ContactChanelEnumEntity.email;
                            case TELEGRAM -> ContactChanelEnumEntity.telegram;
                            case TWITTER -> ContactChanelEnumEntity.twitter;
                            case DISCORD -> ContactChanelEnumEntity.discord;
                            case LINKEDIN -> ContactChanelEnumEntity.linkedin;
                            case WHATSAPP -> ContactChanelEnumEntity.whatsapp;
                        })
                        .build())
                .contact(contact.getContact())
                .isPublic(switch (contact.getVisibility()) {
                    case PRIVATE -> false;
                    case PUBLIC -> true;
                })
                .build()
        ).collect(Collectors.toList());
    }

    static ProfileCoverEnumEntity mapCoverToEntity(UserProfileCover cover) {
        return isNull(cover) ? null : switch (cover) {
            case MAGENTA -> ProfileCoverEnumEntity.magenta;
            case CYAN -> ProfileCoverEnumEntity.cyan;
            case BLUE -> ProfileCoverEnumEntity.blue;
            case YELLOW -> ProfileCoverEnumEntity.yellow;
        };
    }
}
