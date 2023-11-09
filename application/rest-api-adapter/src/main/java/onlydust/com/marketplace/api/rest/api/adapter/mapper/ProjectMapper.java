package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.ProjectMoreInfoLink;
import onlydust.com.marketplace.api.domain.model.UpdateProjectCommand;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;

public interface ProjectMapper {
    static CreateProjectCommand mapCreateProjectCommandToDomain(CreateProjectRequest createProjectRequest,
                                                                UUID authenticatedUserId) {
        return CreateProjectCommand.builder()
                .name(createProjectRequest.getName())
                .shortDescription(createProjectRequest.getShortDescription())
                .longDescription(createProjectRequest.getLongDescription())
                .firstProjectLeaderId(authenticatedUserId)
                .githubUserIdsAsProjectLeadersToInvite(createProjectRequest.getInviteGithubUserIdsAsProjectLeads())
                .githubRepoIds(createProjectRequest.getGithubRepoIds())
                .isLookingForContributors(createProjectRequest.getIsLookingForContributors())
                .moreInfos(createProjectRequest.getMoreInfo().stream()
                        .map(moreInfo -> ProjectMoreInfoLink.builder()
                                .url(moreInfo.getUrl()).value(moreInfo.getValue()).build()).toList())
                .imageUrl(createProjectRequest.getLogoUrl())
                .build();
    }

    static UpdateProjectCommand mapUpdateProjectCommandToDomain(UUID projectId,
                                                                UpdateProjectRequest updateProjectRequest) {
        return UpdateProjectCommand.builder()
                .id(projectId)
                .name(updateProjectRequest.getName())
                .shortDescription(updateProjectRequest.getShortDescription())
                .longDescription(updateProjectRequest.getLongDescription())
                .projectLeadersToKeep(updateProjectRequest.getProjectLeadsToKeep())
                .githubUserIdsAsProjectLeadersToInvite(updateProjectRequest.getInviteGithubUserIdsAsProjectLeads())
                .rewardSettings(mapRewardSettingsToDomain(updateProjectRequest.getRewardSettings()))
                .githubRepoIds(updateProjectRequest.getGithubRepoIds())
                .isLookingForContributors(updateProjectRequest.getIsLookingForContributors())
                .moreInfos(updateProjectRequest.getMoreInfo().stream()
                        .map(moreInfo -> ProjectMoreInfoLink.builder()
                                .url(moreInfo.getUrl()).value(moreInfo.getValue()).build()).toList())
                .imageUrl(updateProjectRequest.getLogoUrl())
                .build();
    }

    static ProjectResponse mapProjectDetails(final ProjectDetailsView project, final boolean includeAllAvailableRepos) {
        final ProjectResponse projectResponse = mapProjectDetailsMetadata(project);
        projectResponse.setTopContributors(project.getTopContributors().stream().map(ProjectMapper::mapUserLink).collect(Collectors.toList()));
        projectResponse.setLeaders(project.getLeaders().stream().map(ProjectMapper::mapUserLinkToRegisteredUserLink).collect(Collectors.toList()));
        projectResponse.setInvitedLeaders(project.getInvitedLeaders().stream().map(ProjectMapper::mapUserLinkToRegisteredUserLink).collect(Collectors.toList()));
        projectResponse.setSponsors(project.getSponsors().stream().map(ProjectMapper::mapSponsor).collect(Collectors.toList()));
        projectResponse.setOrganizations(project.getOrganizations().stream()
                .map(organizationView -> mapOrganization(organizationView, includeAllAvailableRepos)).collect(Collectors.toList()));
        projectResponse.setTechnologies(project.getTechnologies());

        //TODO: this list is kept for backwards compatibility with the old API
        final var repos = new ArrayList<GithubRepoResponse>();
        for (ProjectOrganizationView organization : project.getOrganizations()) {
            repos.addAll(organization.getRepos().stream()
                    .filter(ProjectOrganizationRepoView::getIsIncludedInProject)
                    .map(ProjectMapper::mapRepo)
                    .toList());
        }
        projectResponse.setRepos(repos);

        return projectResponse;
    }

    static ProjectGithubOrganizationResponse mapOrganization(ProjectOrganizationView projectOrganizationView,
                                                             final boolean includeAllAvailableRepos) {
        final var organization = new ProjectGithubOrganizationResponse();
        organization.setId(projectOrganizationView.getId());
        organization.setLogin(projectOrganizationView.getLogin());
        organization.setAvatarUrl(projectOrganizationView.getAvatarUrl());
        organization.setHtmlUrl(projectOrganizationView.getHtmlUrl());
        organization.setName(projectOrganizationView.getName());
        organization.setRepos(projectOrganizationView.getRepos().stream()
                .filter(projectOrganizationRepoView -> includeAllAvailableRepos || projectOrganizationRepoView.getIsIncludedInProject())
                .map(ProjectMapper::mapOrganizationRepo)
                .toList());
        return organization;
    }

    private static ProjectResponse mapProjectDetailsMetadata(final ProjectDetailsView projectDetailsView) {
        final var project = new ProjectResponse();
        project.setId(projectDetailsView.getId());
        project.setSlug(projectDetailsView.getSlug());
        project.setName(projectDetailsView.getName());
        project.setCreatedAt(toZoneDateTime(projectDetailsView.getCreatedAt()));
        project.setShortDescription(projectDetailsView.getShortDescription());
        project.setLongDescription(projectDetailsView.getLongDescription());
        project.setLogoUrl(projectDetailsView.getLogoUrl());
        project.setMoreInfoUrl(projectDetailsView.getMoreInfoUrl());
        project.setHiring(projectDetailsView.getHiring());
        project.setVisibility(mapProjectVisibility(projectDetailsView.getVisibility()));
        project.setContributorCount(projectDetailsView.getContributorCount());
        project.setRemainingUsdBudget(projectDetailsView.getRemainingUsdBudget());
        project.setRewardSettings(mapRewardSettings(projectDetailsView.getRewardSettings()));
        return project;
    }

    static ProjectRewardSettings mapRewardSettings(onlydust.com.marketplace.api.domain.model.ProjectRewardSettings rewardSettings) {
        final var projectRewardSettings = new ProjectRewardSettings();
        projectRewardSettings.setIgnorePullRequests(rewardSettings.getIgnorePullRequests());
        projectRewardSettings.setIgnoreIssues(rewardSettings.getIgnoreIssues());
        projectRewardSettings.setIgnoreCodeReviews(rewardSettings.getIgnoreCodeReviews());
        projectRewardSettings.setIgnoreContributionsBefore(toZoneDateTime(rewardSettings.getIgnoreContributionsBefore()));
        return projectRewardSettings;
    }

    static onlydust.com.marketplace.api.domain.model.ProjectRewardSettings mapRewardSettingsToDomain(ProjectRewardSettings rewardSettings) {
        if (rewardSettings == null) {
            return null;
        }

        return new onlydust.com.marketplace.api.domain.model.ProjectRewardSettings(
                rewardSettings.getIgnorePullRequests(),
                rewardSettings.getIgnoreIssues(),
                rewardSettings.getIgnoreCodeReviews(),
                isNull(rewardSettings.getIgnoreContributionsBefore()) ? null :
                        Date.from(rewardSettings.getIgnoreContributionsBefore().toInstant())
        );
    }

    static ProjectListResponse mapProjectCards(final Page<ProjectCardView> projectViewPage) {
        final ProjectListResponse projectListResponse = new ProjectListResponse();
        final List<ProjectListItemResponse> projectListItemResponses = new ArrayList<>();
        final Set<String> sponsorsNames = new HashSet<>();
        final Set<String> technologies = new HashSet<>();
        for (ProjectCardView projectCardView : projectViewPage.getContent()) {
            projectListItemResponses.add(mapProjectCard(projectCardView, sponsorsNames, technologies));
        }
        projectListResponse.setProjects(projectListItemResponses);
        projectListResponse.setTechnologies(technologies.stream().sorted().toList());
        projectListResponse.setSponsors(sponsorsNames.stream().sorted().toList());
        return projectListResponse;
    }

    private static ProjectListItemResponse mapProjectCard(ProjectCardView projectCardView, Set<String> sponsorsNames,
                                                          Set<String> technologies) {
        final ProjectListItemResponse projectListItemResponse = mapProjectCardMetadata(projectCardView);

        for (ProjectLeaderLinkView leader : projectCardView.getLeaders()) {
            projectListItemResponse.addLeadersItem(mapUserLinkToRegisteredUserLink(leader));
        }

        for (SponsorView sponsor : projectCardView.getSponsors()) {
            projectListItemResponse.addSponsorsItem(mapSponsor(sponsor));
        }
        sponsorsNames.addAll(projectCardView.getSponsors().stream().map(SponsorView::getName).collect(Collectors.toSet()));

        projectListItemResponse.setTechnologies(projectCardView.getTechnologies());
        technologies.addAll(projectCardView.getTechnologies().keySet());

        return projectListItemResponse;
    }

    private static ProjectListItemResponse mapProjectCardMetadata(final ProjectCardView projectCardView) {
        final ProjectListItemResponse project = new ProjectListItemResponse();
        project.setId(projectCardView.getId());
        project.setName(projectCardView.getName());
        project.setLogoUrl(projectCardView.getLogoUrl());
        project.setSlug(projectCardView.getSlug());
        project.setHiring(projectCardView.getHiring());
        project.setShortDescription(projectCardView.getShortDescription());
        project.setContributorCount(projectCardView.getContributorCount());
        project.setRepoCount(projectCardView.getRepoCount());
        project.setVisibility(mapProjectVisibility(projectCardView.getVisibility()));
        project.setIsInvitedAsProjectLead(projectCardView.getIsInvitedAsProjectLead());
        return project;
    }

    private static SponsorResponse mapSponsor(final SponsorView sponsor) {
        final SponsorResponse sponsorResponse = new SponsorResponse();
        sponsorResponse.setId(sponsor.getId());
        sponsorResponse.setName(sponsor.getName());
        sponsorResponse.setLogoUrl(sponsor.getLogoUrl());
        sponsorResponse.setUrl(sponsor.getUrl());
        return sponsorResponse;
    }

    private static GithubRepoResponse mapRepo(final ProjectOrganizationRepoView repo) {
        final var organizationRepo = mapOrganizationRepo(repo);
        organizationRepo.setIsIncludedInProject(null);
        return organizationRepo;
    }

    private static ProjectGithubOrganizationRepoResponse mapOrganizationRepo(final ProjectOrganizationRepoView repo) {
        final ProjectGithubOrganizationRepoResponse repoResponse = new ProjectGithubOrganizationRepoResponse();
        repoResponse.setId(repo.getGithubRepoId());
        repoResponse.setOwner(repo.getOwner());
        repoResponse.setName(repo.getName());
        repoResponse.setHtmlUrl(repo.getUrl());
        repoResponse.setDescription(repo.getDescription());
        repoResponse.setForkCount(Math.toIntExact(repo.getForkCount()));
        repoResponse.setStars(Math.toIntExact(repo.getStarCount()));
        repoResponse.setHasIssues(repo.getHasIssues());
        repoResponse.setIsIncludedInProject(repo.getIsIncludedInProject());
        return repoResponse;
    }

    private static RegisteredUserLinkResponse mapUserLinkToRegisteredUserLink(final ProjectLeaderLinkView projectLeader) {
        final var userLink = new RegisteredUserLinkResponse();
        userLink.setId(projectLeader.getId());
        userLink.setGithubUserId(projectLeader.getGithubUserId());
        userLink.setAvatarUrl(projectLeader.getAvatarUrl());
        userLink.setLogin(projectLeader.getLogin());
        if (projectLeader.getUrl() != null) {
            userLink.setHtmlUrl(URI.create(projectLeader.getUrl()));
        }
        return userLink;
    }

    private static UserLinkResponse mapUserLink(final UserLinkView userLinkView) {
        final var userLink = new UserLinkResponse();
        userLink.setGithubUserId(userLinkView.getGithubUserId());
        userLink.setAvatarUrl(userLinkView.getAvatarUrl());
        userLink.setLogin(userLinkView.getLogin());
        if (userLinkView.getUrl() != null) {
            userLink.setHtmlUrl(URI.create(userLinkView.getUrl()));
        }
        return userLink;
    }

    static ProjectVisibility mapProjectVisibility(onlydust.com.marketplace.api.domain.model.ProjectVisibility visibility) {
        switch (visibility) {
            case PUBLIC -> {
                return ProjectVisibility.PUBLIC;
            }
            case PRIVATE -> {
                return ProjectVisibility.PRIVATE;
            }
        }
        throw new IllegalArgumentException("Could not map project visibility");
    }

    static ProjectCardView.SortBy mapSortByParameter(final String sort) {
        if (Objects.nonNull(sort)) {
            if (sort.equals("RANK")) {
                return ProjectCardView.SortBy.RANK;
            }
            if (sort.equals("NAME")) {
                return ProjectCardView.SortBy.NAME;
            }
            if (sort.equals("REPO_COUNT")) {
                return ProjectCardView.SortBy.REPOS_COUNT;
            }
            if (sort.equals("CONTRIBUTOR_COUNT")) {
                return ProjectCardView.SortBy.CONTRIBUTORS_COUNT;
            }
        }
        return null;
    }

    static ShortProjectResponse mapShortProjectResponse(Project project) {
        return new ShortProjectResponse()
                .id(project.getId())
                .name(project.getName())
                .logoUrl(project.getLogoUrl())
                .slug(project.getSlug())
                .shortDescription(project.getShortDescription())
                .visibility(mapProjectVisibility(project.getVisibility()));
    }
}
