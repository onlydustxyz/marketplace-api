package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.view.UserProfileView;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;

public interface UserMapper {

    static UserProfileResponse userProfileToResponse(UserProfileView userProfileView) {
        final UserProfileResponse userProfileResponse = new UserProfileResponse();
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
        userProfileResponse.setContacts(contactInformationsToResponse(userProfileView.getContactInformations()));
        userProfileResponse.setStats(userStatsToResponse(userProfileView.getProfileStats()));
        userProfileResponse.setProjects(userProjectsToResponse(userProfileView.getProjectsStats()));
        userProfileResponse.setTechnologies(userProfileView.getTechnologies());
        return userProfileResponse;
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

    static List<ContactInformation> contactInformationsToResponse(final Set<UserProfileView.ContactInformation> contactInformations) {
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

    private static UserProfileResponse.CoverEnum coverToUserProfileResponse(final UserProfileView.Cover cover) {
        return isNull(cover) ? null :
                switch (cover) {
                    case BLUE -> UserProfileResponse.CoverEnum.BLUE;
                    case CYAN -> UserProfileResponse.CoverEnum.CYAN;
                    case MAGENTA -> UserProfileResponse.CoverEnum.MAGENTA;
                    case YELLOW -> UserProfileResponse.CoverEnum.YELLOW;
                };
    }

    static GetMeResponse userToGetMeResponse(final User authenticatedUser) {
        final GetMeResponse getMeResponse = new GetMeResponse();
        getMeResponse.setId(authenticatedUser.getId().toString());
        getMeResponse.setGithubUserId(authenticatedUser.getGithubUserId());
        getMeResponse.setAvatarUrl(authenticatedUser.getAvatarUrl());
        getMeResponse.setLogin(authenticatedUser.getLogin());
        return getMeResponse;
    }
}
