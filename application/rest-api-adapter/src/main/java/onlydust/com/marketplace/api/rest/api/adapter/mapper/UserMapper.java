package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import org.springframework.beans.BeanUtils;

import java.net.URI;
import java.util.List;
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
        
        final PublicUserProfileResponse publicUserProfileResponse = new PublicUserProfileResponse();
        BeanUtils.copyProperties(privateUserProfileResponse, publicUserProfileResponse);
        return publicUserProfileResponse;
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
        userProfileResponse.setFirstContributedAt(toZoneDateTime(userProfileView.getFirstContributedAt()));
        return userProfileResponse;
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
                    userProfileProjects.setSlug(ps.getSlug());
                    return userProfileProjects;
                })
                .toList();
    }

    static UserProfileStats userStatsToResponse(final UserProfileView.ProfileStats profileStats) {
        final UserProfileStats userProfileStats = new UserProfileStats();
        userProfileStats.setContributedProjectCount(profileStats.getContributedProjectCount());
        userProfileStats.setTotalsEarned(totalsEarnedToResponse(profileStats.getTotalsEarned()));
        //TODO userProfileStats.setContributionCountVariationSinceLastWeek(profileStats
        // .getContributionCountVariationSinceLastWeek());
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

    static MyRewardTotalAmountsResponse totalsEarnedToResponse(UserProfileView.TotalsEarned totalsEarned) {
        final MyRewardTotalAmountsResponse response = new MyRewardTotalAmountsResponse();
        response.setTotalAmount(totalsEarned.getTotalDollarsEquivalent());
        for (UserProfileView.TotalEarnedPerCurrency totalEarnedPerCurrency : totalsEarned.getDetails()) {
            final MyRewardAmountResponse myRewardAmountResponse = new MyRewardAmountResponse();
            myRewardAmountResponse.setTotalAmount(totalEarnedPerCurrency.getTotalAmount());
            myRewardAmountResponse.totalDollarsEquivalent(totalEarnedPerCurrency.getTotalDollarsEquivalent());
            if (totalEarnedPerCurrency.getCurrency() != null) {
                myRewardAmountResponse.setCurrency(switch (totalEarnedPerCurrency.getCurrency()) {
                    case Apt -> CurrencyContract.APT;
                    case Op -> CurrencyContract.OP;
                    case Eth -> CurrencyContract.ETH;
                    case Stark -> CurrencyContract.STARK;
                    case Usd -> CurrencyContract.USD;
                });
            }
            response.addDetailsItem(myRewardAmountResponse);
        }
        return response;
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
        getMeResponse.setHasValidPayoutInfos(authenticatedUser.getHasValidPayoutInfos());
        return getMeResponse;
    }
}
