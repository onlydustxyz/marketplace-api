package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.Contact;
import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.project.domain.model.UserProfileCover;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

public interface UserMapper {

    static UserProfile userProfileRequestToDomain(final UserProfileUpdateRequest userProfileRequest) {
        return UserProfile.builder()
                .avatarUrl(userProfileRequest.getAvatarUrl())
                .bio(userProfileRequest.getBio())
                .website(userProfileRequest.getWebsite())
                .location(userProfileRequest.getLocation())
                .contacts(contactToDomain(userProfileRequest.getContacts()))
                .allocatedTimeToContribute(allocatedTimeToDomain(userProfileRequest.getAllocatedTimeToContribute()))
                .isLookingForAJob(userProfileRequest.getIsLookingForAJob())
                .firstName(userProfileRequest.getFirstName())
                .lastName(userProfileRequest.getLastName())
                .joiningReason(isNull(userProfileRequest.getJoiningReason()) ? null : switch (userProfileRequest.getJoiningReason()) {
                    case MAINTAINER -> UserProfile.JoiningReason.MAINTAINER;
                    case CONTRIBUTOR -> UserProfile.JoiningReason.CONTRIBUTOR;
                })
                .joiningGoal(isNull(userProfileRequest.getJoiningGoal()) ? null : switch (userProfileRequest.getJoiningGoal()) {
                    case CHALLENGE -> UserProfile.JoiningGoal.CHALLENGE;
                    case EARN -> UserProfile.JoiningGoal.EARN;
                    case LEARN -> UserProfile.JoiningGoal.LEARN;
                    case NOTORIETY -> UserProfile.JoiningGoal.NOTORIETY;
                })
                .preferredCategoriesIds(userProfileRequest.getPreferredCategories())
                .preferredLanguageIds(userProfileRequest.getPreferredLanguages())
                .build();
    }

    static List<Contact> contactToDomain(List<ContactInformation> contacts) {
        return isNull(contacts) ? null : contacts.stream()
                .map(contactInformation -> {
                    final Contact.Visibility visibility = switch (contactInformation.getVisibility()) {
                        case PUBLIC -> Contact.Visibility.PUBLIC;
                        case PRIVATE -> Contact.Visibility.PRIVATE;
                    };
                    final Contact.Channel channel = switch (contactInformation.getChannel()) {
                        case LINKEDIN -> Contact.Channel.LINKEDIN;
                        case TWITTER -> Contact.Channel.TWITTER;
                        case TELEGRAM -> Contact.Channel.TELEGRAM;
                        case DISCORD -> Contact.Channel.DISCORD;
                        case WHATSAPP -> Contact.Channel.WHATSAPP;
                    };
                    return Contact.builder()
                            .contact(contactInformation.getContact())
                            .channel(channel)
                            .visibility(visibility)
                            .build();
                }).toList();
    }

    static PrivateUserProfileResponse userProfileToPrivateResponse(UserProfileView userProfileView) {
        return new PrivateUserProfileResponse()
                .githubUserId(userProfileView.getGithubId())
                .id(userProfileView.getId())
                .login(userProfileView.getLogin())
                .avatarUrl(userProfileView.getAvatarUrl())
                .bio(userProfileView.getBio())
                .website(userProfileView.getWebsite())
                .location(userProfileView.getLocation())
                .contacts(contactToResponse(userProfileView.getContacts()))
                .allocatedTimeToContribute(allocatedTimeToResponse(userProfileView.getAllocatedTimeToContribute()))
                .isLookingForAJob(userProfileView.getIsLookingForAJob())
                .firstName(userProfileView.getFirstName())
                .lastName(userProfileView.getLastName());
    }

    static AllocatedTime allocatedTimeToResponse(UserAllocatedTimeToContribute allocatedTimeToContribute) {
        return isNull(allocatedTimeToContribute) ? null : switch (allocatedTimeToContribute) {
            case NONE -> AllocatedTime.NONE;
            case LESS_THAN_ONE_DAY -> AllocatedTime.LESS_THAN_ONE_DAY;
            case ONE_TO_THREE_DAYS -> AllocatedTime.ONE_TO_THREE_DAYS;
            case GREATER_THAN_THREE_DAYS -> AllocatedTime.GREATER_THAN_THREE_DAYS;
        };
    }

    static UserAllocatedTimeToContribute allocatedTimeToDomain(AllocatedTime allocatedTimeToContribute) {
        return isNull(allocatedTimeToContribute) ? null : switch (allocatedTimeToContribute) {
            case NONE -> UserAllocatedTimeToContribute.NONE;
            case LESS_THAN_ONE_DAY -> UserAllocatedTimeToContribute.LESS_THAN_ONE_DAY;
            case ONE_TO_THREE_DAYS -> UserAllocatedTimeToContribute.ONE_TO_THREE_DAYS;
            case GREATER_THAN_THREE_DAYS -> UserAllocatedTimeToContribute.GREATER_THAN_THREE_DAYS;
        };
    }

    static List<ContactInformation> contactToResponse(final Set<Contact> contacts) {
        return contacts.stream()
                .map(contactInformation -> {
                    final ContactInformation response = new ContactInformation();
                    response.setContact(contactInformation.getContact());
                    response.setChannel(switch (contactInformation.getChannel()) {
                        case LINKEDIN -> ContactInformationChannel.LINKEDIN;
                        case TWITTER -> ContactInformationChannel.TWITTER;
                        case TELEGRAM -> ContactInformationChannel.TELEGRAM;
                        case DISCORD -> ContactInformationChannel.DISCORD;
                        case WHATSAPP -> ContactInformationChannel.WHATSAPP;
                    });
                    response.setVisibility(switch (contactInformation.getVisibility()) {
                        case PUBLIC -> ContactInformation.VisibilityEnum.PUBLIC;
                        case PRIVATE -> ContactInformation.VisibilityEnum.PRIVATE;
                    });
                    return response;
                }).toList();
    }

    static UserProfileCoverColor coverToUserProfileResponse(final @NonNull UserProfileCover cover) {
        return switch (cover) {
            case BLUE -> UserProfileCoverColor.BLUE;
            case CYAN -> UserProfileCoverColor.CYAN;
            case MAGENTA -> UserProfileCoverColor.MAGENTA;
            case YELLOW -> UserProfileCoverColor.YELLOW;
        };
    }

    static ProjectApplicationShortResponse map(final AuthenticatedUser authenticatedUser, final Application application) {
        return new ProjectApplicationShortResponse()
                .id(application.id().value())
                .applicant(map(authenticatedUser))
                .motivations(application.motivations())
                .problemSolvingApproach(application.problemSolvingApproach());
    }

    static ContributorResponse map(AuthenticatedUser authenticatedUser) {
        return new ContributorResponse()
                .githubUserId(authenticatedUser.githubUserId())
                .login(authenticatedUser.login())
                .avatarUrl(authenticatedUser.avatarUrl())
                .isRegistered(true);
    }
}
