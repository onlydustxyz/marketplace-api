package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

public interface UserMapper {

    static UserProfile userProfileRequestToDomain(final UserProfileRequest userProfileRequest) {
        return UserProfile.builder()
                .avatarUrl(userProfileRequest.getAvatarUrl())
                .bio(userProfileRequest.getBio())
                .website(userProfileRequest.getWebsite())
                .location(userProfileRequest.getLocation())
                .contacts(contactToDomain(userProfileRequest.getContacts()))
                .technologies(userProfileRequest.getTechnologies())
                .allocatedTimeToContribute(allocatedTimeToDomain(userProfileRequest.getAllocatedTimeToContribute()))
                .isLookingForAJob(userProfileRequest.getIsLookingForAJob())
                .firstName(userProfileRequest.getFirstName())
                .lastName(userProfileRequest.getLastName())
                .build();
    }

    static List<Contact> contactToDomain(List<ContactInformation> contacts) {
        return contacts.stream()
                .map(contactInformation -> {
                    final Contact.Visibility visibility = switch (contactInformation.getVisibility()) {
                        case PUBLIC -> Contact.Visibility.PUBLIC;
                        case PRIVATE -> Contact.Visibility.PRIVATE;
                    };
                    final Contact.Channel channel = switch (contactInformation.getChannel()) {
                        case EMAIL -> Contact.Channel.EMAIL;
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
                .technologies(userProfileView.getTechnologies())
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
                        case EMAIL -> ContactInformationChannel.EMAIL;
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

    static ProjectApplicationShortResponse map(final User authenticatedUser, final Application application) {
        return new ProjectApplicationShortResponse()
                .id(application.id().value())
                .applicant(map(authenticatedUser))
                .motivations(application.motivations())
                .problemSolvingApproach(application.problemSolvingApproach());
    }

    static ContributorResponse map(User authenticatedUser) {
        return new ContributorResponse()
                .githubUserId(authenticatedUser.getGithubUserId())
                .login(authenticatedUser.getGithubLogin())
                .avatarUrl(authenticatedUser.getGithubAvatarUrl())
                .isRegistered(true);
    }
}
