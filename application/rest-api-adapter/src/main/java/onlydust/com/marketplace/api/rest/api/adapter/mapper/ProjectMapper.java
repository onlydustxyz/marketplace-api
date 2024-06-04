package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.UpdateProjectCommand;
import onlydust.com.marketplace.project.domain.view.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
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
                        .map(moreInfo -> NamedLink.builder()
                                .url(moreInfo.getUrl()).value(moreInfo.getValue()).build()).toList() : null)
                .imageUrl(createProjectRequest.getLogoUrl())
                .ecosystemIds(createProjectRequest.getEcosystemIds())
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
                .moreInfos(isNull(updateProjectRequest.getMoreInfos()) ? null :
                        updateProjectRequest.getMoreInfos().stream()
                                .map(moreInfo -> NamedLink.builder()
                                        .url(moreInfo.getUrl()).value(moreInfo.getValue()).build()).toList())
                .imageUrl(updateProjectRequest.getLogoUrl())
                .ecosystemIds(updateProjectRequest.getEcosystemIds())
                .build();
    }

    static ProjectResponse mapProjectDetails(final ProjectDetailsView project, final boolean includeAllAvailableRepos) {
        final ProjectResponse projectResponse = mapProjectDetailsMetadata(project);
        projectResponse.setTopContributors(project.getTopContributors().stream().map(ProjectMapper::mapGithubUser).toList());
        projectResponse.setLeaders(project.getLeaders().stream().map(ProjectMapper::mapRegisteredUser).collect(Collectors.toList()));
        projectResponse.setInvitedLeaders(project.getInvitedLeaders().stream()
                .map(ProjectMapper::mapRegisteredUser)
                .sorted(comparing(RegisteredUserResponse::getGithubUserId))
                .collect(Collectors.toList()));
        projectResponse.setTags(project.getTags().stream()
                .map(ProjectMapper::mapTag)
                .sorted(comparing(ProjectTag::name))
                .toList());
        projectResponse.setEcosystems(project.getEcosystems().stream()
                .map(ProjectMapper::mapEcosystem)
                .sorted(comparing(EcosystemResponse::getName))
                .toList());
        projectResponse.setSponsors(project.getActiveSponsors().stream()
                .map(ProjectMapper::mapSponsor)
                .sorted(comparing(SponsorResponse::getName))
                .toList());
        projectResponse.setOrganizations(project.getOrganizations().stream()
                .map(organizationView -> mapOrganization(organizationView, includeAllAvailableRepos))
                .sorted(comparing(GithubOrganizationResponse::getGithubUserId))
                .toList());
        projectResponse.setTechnologies(project.getTechnologies());

        final var reposIndexedTimes =
                project.getOrganizations().stream().map(ProjectOrganizationView::getRepos)
                        .flatMap(Collection::stream)
                        .filter(ProjectOrganizationRepoView::getIsIncludedInProject)
                        .map(ProjectOrganizationRepoView::getIndexedAt).toList();
        projectResponse.setIndexingComplete(reposIndexedTimes.stream().noneMatch(Objects::isNull));
        projectResponse.setIndexedAt(reposIndexedTimes.stream().filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null));
        if (project.getMe() != null)
            projectResponse.setMe(new ProjectMeResponse()
                    .isMember(project.getMe().isMember())
                    .isProjectLead(project.getMe().isLeader())
                    .isInvitedAsProjectLead(project.getMe().isInvitedAsProjectLead())
                    .isContributor(project.getMe().isContributor())
                    .hasApplied(project.getMe().hasApplied()));

        return projectResponse;
    }

    static GithubOrganizationResponse mapOrganization(ProjectOrganizationView projectOrganizationView,
                                                      final boolean includeAllAvailableRepos) {
        final var organization = new GithubOrganizationResponse();
        organization.setGithubUserId(projectOrganizationView.getId());
        organization.setLogin(projectOrganizationView.getLogin());
        organization.setAvatarUrl(projectOrganizationView.getAvatarUrl());
        organization.setHtmlUrl(nonNull(projectOrganizationView.getHtmlUrl()) ?
                URI.create(projectOrganizationView.getHtmlUrl()) : null);
        organization.setName(projectOrganizationView.getName());
        organization.setInstallationId(projectOrganizationView.getInstallationId());
        organization.setInstalled(projectOrganizationView.getIsInstalled());
        organization.setRepos(projectOrganizationView.getRepos().stream()
                .filter(projectOrganizationRepoView -> includeAllAvailableRepos || projectOrganizationRepoView.getIsIncludedInProject())
                .map(ProjectMapper::mapOrganizationRepo)
                .sorted(comparing(GithubRepoResponse::getId))
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
        project.setMoreInfos(isNull(projectDetailsView.getMoreInfos()) ? null :
                projectDetailsView.getMoreInfos().stream()
                        .map(moreInfo -> new SimpleLink().url(moreInfo.getUrl()).value(moreInfo.getValue())).collect(Collectors.toList()));
        project.setHiring(projectDetailsView.getHiring());
        project.setVisibility(mapProjectVisibility(projectDetailsView.getVisibility()));
        project.setContributorCount(projectDetailsView.getContributorCount());
        project.setHasRemainingBudget(projectDetailsView.getHasRemainingBudget());
        project.setRewardSettings(mapRewardSettings(projectDetailsView.getRewardSettings()));
        return project;
    }

    static ProjectRewardSettings mapRewardSettings(onlydust.com.marketplace.project.domain.model.ProjectRewardSettings rewardSettings) {
        final var projectRewardSettings = new ProjectRewardSettings();
        projectRewardSettings.setIgnorePullRequests(rewardSettings.getIgnorePullRequests());
        projectRewardSettings.setIgnoreIssues(rewardSettings.getIgnoreIssues());
        projectRewardSettings.setIgnoreCodeReviews(rewardSettings.getIgnoreCodeReviews());
        projectRewardSettings.setIgnoreContributionsBefore(toZoneDateTime(rewardSettings.getIgnoreContributionsBefore()));
        return projectRewardSettings;
    }

    static onlydust.com.marketplace.project.domain.model.ProjectRewardSettings mapRewardSettingsToDomain(ProjectRewardSettings rewardSettings) {
        if (rewardSettings == null) {
            return null;
        }

        return new onlydust.com.marketplace.project.domain.model.ProjectRewardSettings(
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
        final Set<EcosystemResponse> ecosystems = page.getFilters().get(ProjectCardView.FilterBy.ECOSYSTEMS.name())
                .stream()
                .map(EcosystemView.class::cast)
                .map(ProjectMapper::mapEcosystem)
                .collect(Collectors.toSet());
        for (ProjectCardView projectCardView : page.getContent()) {
            projectPageItemResponses.add(mapProjectCard(projectCardView));
        }
        projectPageResponse.setProjects(projectPageItemResponses);
        projectPageResponse.setEcosystems(ecosystems.stream().sorted(comparing(EcosystemResponse::getName)).toList());
        projectPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        projectPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        projectPageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        projectPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        return projectPageResponse;
    }

    private static ProjectPageItemResponse mapProjectCard(ProjectCardView projectCardView) {
        final ProjectPageItemResponse projectListItemResponse = mapProjectCardMetadata(projectCardView);
        for (ProjectLeaderLinkView leader : projectCardView.getLeaders()) {
            projectListItemResponse.addLeadersItem(mapRegisteredUser(leader));
        }
        for (EcosystemView ecosystemView : projectCardView.getEcosystems()) {
            projectListItemResponse.addEcosystemsItem(mapEcosystem(ecosystemView));
        }
        for (LanguageView language : projectCardView.getLanguages()) {
            projectListItemResponse.addLanguagesItem(mapLanguage(language));
        }
        return projectListItemResponse;
    }

    static LanguageResponse mapLanguage(LanguageView view) {
        return new LanguageResponse()
                .id(view.getId())
                .name(view.getName())
                .logoUrl(view.getLogoUrl())
                .bannerUrl(view.getBannerUrl())
                ;
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
        project.setHasMissingGithubAppInstallation(projectCardView.getIsMissingGithubAppInstallation());
        project.setTags(projectCardView.getTags().stream().map(ProjectMapper::mapTag).toList());
        project.setLanguages(projectCardView.getLanguages().stream().map(ProjectMapper::mapLanguage).toList());
        return project;
    }

    private static ProjectTag mapTag(final Project.Tag tag) {
        return switch (tag) {
            case HOT_COMMUNITY -> ProjectTag.HOT_COMMUNITY;
            case UPDATED_ROADMAP -> ProjectTag.UPDATED_ROADMAP;
            case BIG_WHALE -> ProjectTag.BIG_WHALE;
            case FAST_AND_FURIOUS -> ProjectTag.FAST_AND_FURIOUS;
            case LIKELY_TO_REWARD -> ProjectTag.LIKELY_TO_REWARD;
            case NEWBIES_WELCOME -> ProjectTag.NEWBIES_WELCOME;
            case WORK_IN_PROGRESS -> ProjectTag.WORK_IN_PROGRESS;
        };
    }

    private static EcosystemResponse mapEcosystem(final EcosystemView ecosystem) {
        final EcosystemResponse ecosystemResponse = new EcosystemResponse();
        ecosystemResponse.setId(ecosystem.getId());
        ecosystemResponse.setName(ecosystem.getName());
        ecosystemResponse.setLogoUrl(ecosystem.getLogoUrl());
        ecosystemResponse.setUrl(ecosystem.getUrl());
        ecosystemResponse.setSlug(ecosystem.getSlug());
        return ecosystemResponse;
    }

    private static SponsorResponse mapSponsor(final ProjectSponsorView projectSponsorView) {
        final SponsorResponse sponsorResponse = new SponsorResponse();
        sponsorResponse.setId(projectSponsorView.sponsorId());
        sponsorResponse.setName(projectSponsorView.sponsorName());
        sponsorResponse.setLogoUrl(projectSponsorView.sponsorLogoUrl());
        sponsorResponse.setUrl(projectSponsorView.sponsorUrl());
        return sponsorResponse;
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

    private static RegisteredUserResponse mapRegisteredUser(final ProjectLeaderLinkView projectLeader) {
        final var user = new RegisteredUserResponse();
        user.setId(projectLeader.getId());
        user.setGithubUserId(projectLeader.getGithubUserId());
        user.setAvatarUrl(projectLeader.getAvatarUrl());
        user.setLogin(projectLeader.getLogin());
        return user;
    }

    static GithubUserResponse mapGithubUser(final UserLinkView userLinkView) {
        final var user = new GithubUserResponse();
        user.setGithubUserId(userLinkView.getGithubUserId());
        user.setAvatarUrl(userLinkView.getAvatarUrl());
        user.setLogin(userLinkView.getLogin());
        return user;
    }

    static ProjectVisibility mapProjectVisibility(onlydust.com.marketplace.project.domain.model.ProjectVisibility visibility) {
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

    static List<Project.Tag> mapTagsParameter(final List<ProjectTag> projectTags) {
        return isNull(projectTags) ? null : projectTags.stream()
                .map(ProjectMapper::mapTagParameter)
                .toList();
    }

    private static Project.Tag mapTagParameter(final ProjectTag projectTag) {
        return switch (projectTag) {
            case BIG_WHALE -> Project.Tag.BIG_WHALE;
            case FAST_AND_FURIOUS -> Project.Tag.FAST_AND_FURIOUS;
            case HOT_COMMUNITY -> Project.Tag.HOT_COMMUNITY;
            case LIKELY_TO_REWARD -> Project.Tag.LIKELY_TO_REWARD;
            case NEWBIES_WELCOME -> Project.Tag.NEWBIES_WELCOME;
            case UPDATED_ROADMAP -> Project.Tag.UPDATED_ROADMAP;
            case WORK_IN_PROGRESS -> Project.Tag.WORK_IN_PROGRESS;
        };
    }

    static ProjectShortResponse mapShortProjectResponse(Project project) {
        return new ProjectShortResponse()
                .id(project.getId())
                .name(project.getName())
                .logoUrl(project.getLogoUrl())
                .slug(project.getSlug())
                .shortDescription(project.getShortDescription())
                .visibility(mapProjectVisibility(project.getVisibility()));
    }
}
