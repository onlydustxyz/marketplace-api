package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.enums.AllocatedTimeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ContactInformationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserProfileInfoEntity;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.Contact;
import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.project.domain.model.UserProfile;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface UserMapper {

    static AuthenticatedUser mapUserToDomain(UserViewEntity user, List<ProjectLedIdQueryEntity> projectLedIdViewEntities,
                                             List<BillingProfileUserEntity> billingProfiles) {
        return AuthenticatedUser.builder()
                .id(user.id())
                .githubUserId(user.githubUserId())
                .login(user.login())
                .email(user.email())
                .avatarUrl(user.profile() != null && user.profile().avatarUrl() != null ?
                        user.profile().avatarUrl() : user.avatarUrl())
                .roles(Arrays.stream(user.roles()).toList())
                .projectsLed(projectLedIdViewEntities.stream()
                        .filter(projectLedIdQueryEntity -> !projectLedIdQueryEntity.getPending())
                        .map(projectLedIdQueryEntity -> projectLedIdQueryEntity.getId().getProjectId()).toList())
                .billingProfiles(billingProfiles.stream()
                        .map(bp -> new AuthenticatedUser.BillingProfileMembership(bp.getBillingProfileId(), bp.getRole().toBillingProfileMembershipRole()))
                        .toList())
                .build();
    }

    static AuthenticatedUser mapCreatedUserToDomain(final UserEntity userEntity) {
        return AuthenticatedUser.builder()
                .id(userEntity.getId())
                .githubUserId(userEntity.getGithubUserId())
                .login(userEntity.getGithubLogin())
                .email(userEntity.getEmail())
                .avatarUrl(userEntity.getGithubAvatarUrl())
                .roles(Arrays.stream(userEntity.getRoles()).toList())
                .build();

    }

    static UserEntity mapUserToEntity(AuthenticatedUser user) {
        return UserEntity.builder()
                .id(user.id())
                .githubUserId(user.githubUserId())
                .githubLogin(user.login())
                .githubAvatarUrl(user.avatarUrl())
                .email(user.email())
                .roles(user.roles() != null ? user.roles().toArray(AuthenticatedUser.Role[]::new) : new AuthenticatedUser.Role[0])
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
