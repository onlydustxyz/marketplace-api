package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.enums.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ContactInformationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserProfileInfoEntity;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.Contact;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import onlydust.com.marketplace.project.domain.view.ProjectLedView;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface UserMapper {

    static User mapUserToDomain(UserViewEntity user, List<ProjectLedIdQueryEntity> projectLedIdViewEntities,
                                List<BillingProfileLinkView> billingProfiles) {
        return User.builder()
                .id(user.id())
                .githubUserId(user.githubUserId())
                .githubLogin(user.login())
                .email(user.email())
                .githubAvatarUrl(user.profile() != null && user.profile().avatarUrl() != null ?
                        user.profile().avatarUrl() : user.avatarUrl())
                .roles(Arrays.stream(user.roles()).toList())
                .projectsLed(projectLedIdViewEntities.stream()
                        .filter(projectLedIdQueryEntity -> !projectLedIdQueryEntity.getPending())
                        .map(projectLedIdQueryEntity -> ProjectLedView.builder()
                                .name(projectLedIdQueryEntity.getName())
                                .logoUrl(projectLedIdQueryEntity.getLogoUrl())
                                .slug(projectLedIdQueryEntity.getProjectSlug())
                                .id(projectLedIdQueryEntity.getId().getProjectId())
                                .contributorCount(projectLedIdQueryEntity.getContributorCount())
                                .hasMissingGithubAppInstallation(projectLedIdQueryEntity.getIsMissingGithubAppInstallation())
                                .build()).toList())
                .billingProfiles(billingProfiles)
                .build();
    }

    static User mapCreatedUserToDomain(final UserEntity userEntity) {
        return User.builder()
                .id(userEntity.getId())
                .githubUserId(userEntity.getGithubUserId())
                .githubLogin(userEntity.getGithubLogin())
                .email(userEntity.getEmail())
                .githubAvatarUrl(userEntity.getGithubAvatarUrl())
                .roles(Arrays.stream(userEntity.getRoles()).toList())
                .build();

    }

    static UserEntity mapUserToEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .githubLogin(user.getGithubLogin())
                .githubAvatarUrl(user.getGithubAvatarUrl())
                .email(user.getEmail())
                .roles(user.getRoles() != null ? user.getRoles().toArray(AuthenticatedUser.Role[]::new) : new AuthenticatedUser.Role[0])
                .lastSeenAt(new Date())
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

    static Set<ContactInformationEntity> mapContactInformationsToEntity(UUID userId, List<Contact> contacts) {
        return isNull(contacts) ? null : contacts.stream().map(contact -> ContactInformationEntity.builder()
                .userId(userId)
                .channel(contact.getChannel())
                .contact(contact.getContact())
                .isPublic(switch (contact.getVisibility()) {
                    case PRIVATE -> false;
                    case PUBLIC -> true;
                })
                .build()
        ).collect(Collectors.toSet());
    }
}
