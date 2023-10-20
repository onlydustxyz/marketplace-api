package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.UserProfileView;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;

public interface UserMapper {

    static UserProfile userProfileRequestToDomain(final UserProfileRequest userProfileRequest) {
        return UserProfile.builder()
                .bio(userProfileRequest.getBio())
                .website(userProfileRequest.getWebsite())
                .location(userProfileRequest.getLocation())
                .cover(coverToUserProfileDomain(userProfileRequest.getCover()))
                .contacts(contactToDomain(userProfileRequest.getContacts()))
                .technologies(userProfileRequest.getTechnologies())
                .allocatedTimeToContribute(allocatedTimeToDomain(userProfileRequest.getAllocatedTimeToContribute()))
                .isLookingForAJob(userProfileRequest.getIsLookingForAJob())
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

    static UserProfileCover coverToUserProfileDomain(UserProfileCoverColor cover) {
        return isNull(cover) ? UserProfileCover.BLUE :
                switch (cover) {
                    case BLUE -> UserProfileCover.BLUE;
                    case CYAN -> UserProfileCover.CYAN;
                    case MAGENTA -> UserProfileCover.MAGENTA;
                    case YELLOW -> UserProfileCover.YELLOW;
                };
    }


    static PublicUserProfileResponse userProfileToPublicResponse(UserProfileView userProfileView) {
        final PrivateUserProfileResponse privateUserProfileResponse = userProfileToPrivateResponse(userProfileView);
        privateUserProfileResponse.setIsLookingForAJob(null);
        privateUserProfileResponse.setAllocatedTimeToContribute(null);
        privateUserProfileResponse.setContacts(
                privateUserProfileResponse.getContacts().stream().filter(contact -> contact.getVisibility() == ContactInformation.VisibilityEnum.PUBLIC).collect(Collectors.toList())
        );
        return privateUserProfileResponse;
    }

    static PrivateUserProfileResponse userProfileToPrivateResponse(UserProfileView userProfileView) {
        final PrivateUserProfileResponse userProfileResponse = new PrivateUserProfileResponse();
        userProfileResponse.setGithubUserId(userProfileView.getGithubId());
        userProfileResponse.setBio(userProfileView.getBio());
        userProfileResponse.setAvatarUrl(userProfileView.getAvatarUrl());
        userProfileResponse.setId(userProfileView.getId());
        userProfileResponse.setLogin(userProfileView.getLogin());
        userProfileResponse.setAvatarUrl(userProfileView.getAvatarUrl());
        userProfileResponse.setWebsite(userProfileView.getWebsite());
        userProfileResponse.setHtmlUrl((isNull(userProfileView.getHtmlUrl()) ? null :
                URI.create(userProfileView.getHtmlUrl())));
        userProfileResponse.setCover(coverToUserProfileResponse(userProfileView.getCover()));
        userProfileResponse.setCreatedAt(toZoneDateTime(userProfileView.getCreateAt()));
        userProfileResponse.setLastSeenAt(toZoneDateTime(userProfileView.getLastSeenAt()));
        userProfileResponse.setLocation(userProfileView.getLocation());
        userProfileResponse.setContacts(contactToResponse(userProfileView.getContacts()));
        userProfileResponse.setStats(userStatsToResponse(userProfileView.getProfileStats()));
        userProfileResponse.setProjects(userProjectsToResponse(userProfileView.getProjectsStats()));
        userProfileResponse.setTechnologies(userProfileView.getTechnologies());
        userProfileResponse.setAllocatedTimeToContribute(allocatedTimeToResponse(userProfileView.getAllocatedTimeToContribute()));
        userProfileResponse.setIsLookingForAJob(userProfileView.getIsLookingForAJob());
        return userProfileResponse;
    }

    static AllocatedTime allocatedTimeToResponse(UserAllocatedTimeToContribute allocatedTimeToContribute) {
        return switch (allocatedTimeToContribute) {
            case NONE -> AllocatedTime.NONE;
            case LESS_THAN_ONE_DAY -> AllocatedTime.LESS_THAN_ONE_DAY;
            case ONE_TO_THREE_DAYS -> AllocatedTime.ONE_TO_THREE_DAYS;
            case GREATER_THAN_THREE_DAYS -> AllocatedTime.GREATER_THAN_THREE_DAYS;
        };
    }

    static UserAllocatedTimeToContribute allocatedTimeToDomain(AllocatedTime allocatedTimeToContribute) {
        return switch (allocatedTimeToContribute) {
            case NONE -> UserAllocatedTimeToContribute.NONE;
            case LESS_THAN_ONE_DAY -> UserAllocatedTimeToContribute.LESS_THAN_ONE_DAY;
            case ONE_TO_THREE_DAYS -> UserAllocatedTimeToContribute.ONE_TO_THREE_DAYS;
            case GREATER_THAN_THREE_DAYS -> UserAllocatedTimeToContribute.GREATER_THAN_THREE_DAYS;
        };
    }

    static List<UserProfileProjects> userProjectsToResponse(final Set<UserProfileView.ProjectStats> projectStats) {
        return projectStats.stream()
                .map(ps -> {
                    final UserProfileProjects userProfileProjects = new UserProfileProjects();
                    userProfileProjects.setUserContributionCount(ps.getUserContributionCount());
                    userProfileProjects.setId(ps.getId());
                    userProfileProjects.setName(ps.getName());
                    userProfileProjects.setTotalGranted(ps.getTotalGranted());
                    userProfileProjects.setLogoUrl(ps.getLogoUrl());
                    userProfileProjects.setContributorCount(ps.getContributorCount());
                    userProfileProjects.setUserLastContributedAt(toZoneDateTime(ps.getUserLastContributedAt()));
                    userProfileProjects.setIsLead(ps.getIsProjectLead());
                    return userProfileProjects;
                })
                .toList();
    }

    static UserProfileStats userStatsToResponse(final UserProfileView.ProfileStats profileStats) {
        final UserProfileStats userProfileStats = new UserProfileStats();
        userProfileStats.setContributedProjectCount(profileStats.getContributedProjectCount());
        userProfileStats.setTotalEarned(profileStats.getTotalEarned());
        userProfileStats.setLeadedProjectCount(profileStats.getLeadedProjectCount());
        userProfileStats.setContributionCount(profileStats.getContributionCount());
        userProfileStats.setContributionCountPerWeeks(
                profileStats.getContributionStats()
                        .stream()
                        .map(contributionStats -> {
                            final UserContributionStats userContributionStats = new UserContributionStats();
                            userContributionStats.setCodeReviewCount(contributionStats.getCodeReviewCount());
                            userContributionStats.setIssueCount(contributionStats.getIssueCount());
                            userContributionStats.setPullRequestCount(contributionStats.getPullRequestCount());
                            userContributionStats.setWeek(contributionStats.getWeek());
                            userContributionStats.setYear(contributionStats.getYear());
                            return userContributionStats;
                        }).toList()
        );
        return userProfileStats;
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

    private static UserProfileCoverColor coverToUserProfileResponse(final UserProfileCover cover) {
        return isNull(cover) ? UserProfileCoverColor.BLUE :
                switch (cover) {
                    case BLUE -> UserProfileCoverColor.BLUE;
                    case CYAN -> UserProfileCoverColor.CYAN;
                    case MAGENTA -> UserProfileCoverColor.MAGENTA;
                    case YELLOW -> UserProfileCoverColor.YELLOW;
                };
    }

    static GetMeResponse userToGetMeResponse(final User authenticatedUser) {
        final GetMeResponse getMeResponse = new GetMeResponse();
        getMeResponse.setId(authenticatedUser.getId().toString());
        getMeResponse.setGithubUserId(authenticatedUser.getGithubUserId());
        getMeResponse.setAvatarUrl(authenticatedUser.getAvatarUrl());
        getMeResponse.setLogin(authenticatedUser.getLogin());
        getMeResponse.setHasSeenOnboardingWizard(authenticatedUser.hasSeenOnboardingWizard());
        getMeResponse.setHasAcceptedLatestTermsAndConditions(authenticatedUser.hasAcceptedLatestTermsAndConditions());
        return getMeResponse;
    }

    static UserPayoutInformationContract userPayoutInformationToResponse(UserPayoutInformation view) {
        final UserPayoutInformationContract userPayoutInformation = new UserPayoutInformationContract();
        final Boolean isACompany = view.getIsACompany();
        userPayoutInformation.setIsCompany(isACompany);
        if (isACompany) {
            final CompanyIdentity company = new CompanyIdentity();
            company.setName(view.getCompany().getName());
            company.setIdentificationNumber(view.getCompany().getIdentificationNumber());
            final PersonIdentity owner = new PersonIdentity();
            owner.setFirstname(view.getCompany().getOwner().getFirstName());
            owner.setLastname(view.getCompany().getOwner().getLastName());
            company.setOwner(owner);
            userPayoutInformation.setCompany(company);
        } else {
            final PersonIdentity person = new PersonIdentity();
            person.setFirstname(view.getPerson().getFirstName());
            person.setLastname(view.getPerson().getLastName());
            userPayoutInformation.setPerson(person);
        }
        final UserPayoutInformationContractPayoutSettings payoutSettings =
                new UserPayoutInformationContractPayoutSettings();
        payoutSettings.setAptosAddress(view.getPayoutSettings().getAptosAddress());
        payoutSettings.setEthAddress(view.getPayoutSettings().getEthAddress());
        payoutSettings.setEthName(view.getPayoutSettings().getEthName());
        payoutSettings.setOptimismAddress(view.getPayoutSettings().getOptimismAddress());
        payoutSettings.setStarknetAddress(view.getPayoutSettings().getStarknetAddress());
        if (Objects.nonNull(view.getPayoutSettings().getSepaAccount())) {
            final UserPayoutInformationContractPayoutSettingsSepaAccount sepaAccount =
                    new UserPayoutInformationContractPayoutSettingsSepaAccount();
            sepaAccount.setBic(view.getPayoutSettings().getSepaAccount().getBic());
            sepaAccount.setIban(view.getPayoutSettings().getSepaAccount().getIban());
            payoutSettings.setSepaAccount(sepaAccount);
        }
        if (Objects.nonNull(view.getPayoutSettings().getUsdPreferredMethodEnum())) {
            switch (view.getPayoutSettings().getUsdPreferredMethodEnum()) {
                case FIAT ->
                        payoutSettings.setUsdPreferredMethod(UserPayoutInformationContractPayoutSettings.UsdPreferredMethodEnum.SEPA);
                case CRYPTO ->
                        payoutSettings.setUsdPreferredMethod(UserPayoutInformationContractPayoutSettings.UsdPreferredMethodEnum.USDC);
            }
        }
        userPayoutInformation.setPayoutSettings(payoutSettings);
        final UserPayoutInformationContractLocation location = new UserPayoutInformationContractLocation();
        location.setAddress(view.getLocation().getAddress());
        location.setCity(view.getLocation().getCity());
        location.setCountry(view.getLocation().getCountry());
        location.setPostalCode(view.getLocation().getPostalCode());
        userPayoutInformation.setLocation(location);
        return userPayoutInformation;
    }
}
