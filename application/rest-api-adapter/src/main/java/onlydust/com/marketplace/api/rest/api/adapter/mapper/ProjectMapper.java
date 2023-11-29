package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.MoreInfoLink;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.UpdateProjectCommand;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
                .moreInfos(nonNull(createProjectRequest.getMoreInfos()) ? createProjectRequest.getMoreInfos().stream()
                        .map(moreInfo -> MoreInfoLink.builder()
                                .url(moreInfo.getUrl()).value(moreInfo.getValue()).build()).toList() : null)
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
                .moreInfos(updateProjectRequest.getMoreInfos().stream()
                        .map(moreInfo -> MoreInfoLink.builder()
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
                .map(organizationView -> mapOrganization(organizationView, includeAllAvailableRepos))
                .sorted(Comparator.comparing(GithubOrganizationResponse::getId))
                .collect(Collectors.toList()));
        projectResponse.setTechnologies(project.getTechnologies());

        //TODO: this list is kept for backwards compatibility with the old API
        final var repos = new ArrayList<GithubRepoResponse>();
        for (ProjectOrganizationView organization : project.getOrganizations()) {
            repos.addAll(organization.getRepos().stream()
                    .filter(ProjectOrganizationRepoView::getIsIncludedInProject)
                    .map(ProjectMapper::mapRepo)
                    .toList());
        }
        repos.sort(Comparator.comparing(GithubRepoResponse::getId));
        projectResponse.setRepos(repos);

        return projectResponse;
    }

    static GithubOrganizationResponse mapOrganization(ProjectOrganizationView projectOrganizationView,
                                                      final boolean includeAllAvailableRepos) {
        final var organization = new GithubOrganizationResponse();
        organization.setId(projectOrganizationView.getId());
        organization.setLogin(projectOrganizationView.getLogin());
        organization.setAvatarUrl(projectOrganizationView.getAvatarUrl());
        organization.setHtmlUrl(projectOrganizationView.getHtmlUrl());
        organization.setName(projectOrganizationView.getName());
        organization.setInstallationId(projectOrganizationView.getInstallationId());
        organization.setInstalled(projectOrganizationView.getIsInstalled());
        organization.setRepos(projectOrganizationView.getRepos().stream()
                .filter(projectOrganizationRepoView -> includeAllAvailableRepos || projectOrganizationRepoView.getIsIncludedInProject())
                .map(ProjectMapper::mapOrganizationRepo)
                .sorted(Comparator.comparing(ShortGithubRepoResponse::getId))
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
        project.setMoreInfos(isNull(projectDetailsView.getMoreInfos()) ? null : projectDetailsView.getMoreInfos().stream()
                .map(moreInfo -> new MoreInfo().url(moreInfo.getUrl()).value(moreInfo.getValue())).collect(Collectors.toList()));
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

    static ProjectPageResponse mapProjectCards(final Page<ProjectCardView> page, final Integer pageIndex) {
        final ProjectPageResponse projectPageResponse = new ProjectPageResponse();
        final List<ProjectPageItemResponse> projectPageItemResponses = new ArrayList<>();
        final Set<String> technologies = page.getFilters().get(ProjectCardView.FilterBy.TECHNOLOGIES.name())
                .stream()
                .map(String.class::cast)
                .collect(Collectors.toSet());
        final Set<SponsorResponse> sponsors = page.getFilters().get(ProjectCardView.FilterBy.SPONSORS.name())
                .stream()
                .map(SponsorView.class::cast)
                .map(ProjectMapper::mapSponsor)
                .collect(Collectors.toSet());
        for (ProjectCardView projectCardView : page.getContent()) {
            projectPageItemResponses.add(mapProjectCard(projectCardView));
        }
        projectPageResponse.setProjects(projectPageItemResponses);
        projectPageResponse.setTechnologies(technologies.stream().sorted().toList());
        projectPageResponse.setSponsors(sponsors.stream().sorted(Comparator.comparing(SponsorResponse::getName)).toList());
        projectPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        projectPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        projectPageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        projectPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        return projectPageResponse;
    }

    private static ProjectPageItemResponse mapProjectCard(ProjectCardView projectCardView) {
        final ProjectPageItemResponse projectListItemResponse = mapProjectCardMetadata(projectCardView);
        for (ProjectLeaderLinkView leader : projectCardView.getLeaders()) {
            projectListItemResponse.addLeadersItem(mapUserLinkToRegisteredUserLink(leader));
        }
        for (SponsorView sponsor : projectCardView.getSponsors()) {
            projectListItemResponse.addSponsorsItem(mapSponsor(sponsor));
        }
        projectListItemResponse.setTechnologies(projectCardView.getTechnologies());
        return projectListItemResponse;
    }

    private static ProjectPageItemResponse mapProjectCardMetadata(final ProjectCardView projectCardView) {
        final ProjectPageItemResponse project = new ProjectPageItemResponse();
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
        project.setIsMissingGithubAppInstallation(projectCardView.getIsMissingGithubAppInstallation());
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

    private static GithubRepoResponse mapOrganizationRepo(final ProjectOrganizationRepoView repo) {
        final GithubRepoResponse repoResponse = new GithubRepoResponse();
        repoResponse.setId(repo.getGithubRepoId());
        repoResponse.setOwner(repo.getOwner());
        repoResponse.setName(repo.getName());
        repoResponse.setHtmlUrl(repo.getUrl());
        repoResponse.setDescription(repo.getDescription());
        repoResponse.setForkCount(isNull(repo.getForkCount()) ? null : Math.toIntExact(repo.getForkCount()));
        repoResponse.setStars(isNull(repo.getStarCount()) ? null : Math.toIntExact(repo.getStarCount()));
        repoResponse.setHasIssues(repo.getHasIssues());
        repoResponse.setIsIncludedInProject(repo.getIsIncludedInProject());
        repoResponse.setIsAuthorizedInGithubApp(repo.getIsAuthorizedInGithubApp());
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

    static UserLinkResponse mapUserLink(final UserLinkView userLinkView) {
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
