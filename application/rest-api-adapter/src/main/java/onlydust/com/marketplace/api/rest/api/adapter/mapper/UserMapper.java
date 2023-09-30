package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.UserProfile;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;

public interface UserMapper {

    static UserProfileResponse userProfileToResponse(UserProfile userProfile) {
        final UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setGithubUserId(userProfile.getGithubId());
        userProfileResponse.setBio(userProfile.getBio());
        userProfileResponse.setAvatarUrl(userProfile.getAvatarUrl());
        userProfileResponse.setId(userProfile.getId());
        userProfileResponse.setLogin(userProfile.getLogin());
        userProfileResponse.setAvatarUrl(userProfile.getAvatarUrl());
        userProfileResponse.setWebsite(userProfile.getWebsite());
        userProfileResponse.setHtmlUrl((isNull(userProfile.getHtmlUrl()) ? null :
                URI.create(userProfile.getHtmlUrl())));
        userProfileResponse.setCover(coverToUserProfileResponse(userProfile.getCover()));
        userProfileResponse.setCreatedAt(toZoneDateTime(userProfile.getCreateAt()));
        userProfileResponse.setLastSeenAt(toZoneDateTime(userProfile.getLastSeenAt()));
        userProfileResponse.setLocation(userProfile.getLocation());
        userProfileResponse.setUpdatedAt(toZoneDateTime(userProfile.getUpdatedAt()));
        userProfileResponse.setContacts(contactInformationsToResponse(userProfile.getContactInformations()));
        userProfileResponse.setStats(userStatsToResponse(userProfile.getProfileStats()));
        userProfileResponse.setProjects(userProjectsToResponse(userProfile.getProjectsStats()));
        userProfileResponse.setTechnologies(userProfile.getTechnologies());
        return userProfileResponse;
    }

    static List<UserProfileProjects> userProjectsToResponse(Set<UserProfile.ProjectStats> projectStats) {
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
                    return userProfileProjects;
                })
                .toList();
    }

    static UserProfileStats userStatsToResponse(UserProfile.ProfileStats profileStats) {
        final UserProfileStats userProfileStats = new UserProfileStats();
        userProfileStats.setContributedProjectCount(profileStats.getContributedProjectCount());
        userProfileStats.setTotalEarned(profileStats.getTotalEarned());
        userProfileStats.setLeadedProjectCount(profileStats.getLeadedProjectCount());
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

    static List<ContactInformation> contactInformationsToResponse(Set<UserProfile.ContactInformation> contactInformations) {
        return contactInformations.stream()
                .map(contactInformation -> {
                    final ContactInformation response = new ContactInformation();
                    response.setContact(contactInformation.getContact());
                    response.setChannel(contactInformation.getChannel());
                    switch (contactInformation.getVisibility()) {
                        case PUBLIC -> response.setVisibility(ContactInformation.VisibilityEnum.PUBLIC);
                        case PRIVATE -> response.setVisibility(ContactInformation.VisibilityEnum.PRIVATE);
                    }
                    return response;
                }).toList();
    }

    private static UserProfileResponse.CoverEnum coverToUserProfileResponse(final UserProfile.Cover cover) {
        return switch (cover) {
            case BLUE -> UserProfileResponse.CoverEnum.BLUE;
            case CYAN -> UserProfileResponse.CoverEnum.CYAN;
            case MAGENTA -> UserProfileResponse.CoverEnum.MAGENTA;
            case YELLOW -> UserProfileResponse.CoverEnum.YELLOW;
        };
    }
}
