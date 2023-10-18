package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public interface ProjectMapper {

    static ProjectResponse mapProjectDetails(final ProjectDetailsView project) {
        final ProjectResponse projectListItemResponse = mapProjectDetailsMetadata(project);
        projectListItemResponse.setTopContributors(project.getTopContributors().stream().map(ProjectMapper::mapUserLink).collect(Collectors.toList()));
        projectListItemResponse.setLeaders(project.getLeaders().stream().map(ProjectMapper::mapUserLinkToRegisteredUserLink).collect(Collectors.toList()));
        projectListItemResponse.setSponsors(project.getSponsors().stream().map(ProjectMapper::mapSponsor).collect(Collectors.toList()));
        projectListItemResponse.setRepos(project.getRepos().stream().map(ProjectMapper::mapRepo).collect(Collectors.toList()));
        projectListItemResponse.setTechnologies(project.getTechnologies());
        return projectListItemResponse;
    }

    private static ProjectResponse mapProjectDetailsMetadata(final ProjectDetailsView projectDetailsView) {
        final var project = new ProjectResponse();
        project.setId(projectDetailsView.getId());
        project.setSlug(projectDetailsView.getSlug());
        project.setName(projectDetailsView.getName());
        project.setShortDescription(projectDetailsView.getShortDescription());
        project.setLongDescription(projectDetailsView.getLongDescription());
        project.setLogoUrl(projectDetailsView.getLogoUrl());
        project.setMoreInfoUrl(projectDetailsView.getMoreInfoUrl());
        project.setHiring(projectDetailsView.getHiring());
        project.setVisibility(mapProjectVisibility(projectDetailsView.getVisibility()));
        project.setContributorCount(projectDetailsView.getContributorCount());
        project.setRemainingUsdBudget(projectDetailsView.getRemainingUsdBudget());
        return project;
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
        return sponsorResponse;
    }

    private static GithubRepoResponse mapRepo(final RepoCardView repo) {
        final GithubRepoResponse repoResponse = new GithubRepoResponse();
        repoResponse.setId(repo.getGithubRepoId());
        repoResponse.setOwner(repo.getOwner());
        repoResponse.setName(repo.getName());
        repoResponse.setHtmlUrl(repo.getUrl());
        repoResponse.setDescription(repo.getDescription());
        repoResponse.setForkCount(repo.getForkCount());
        repoResponse.setStars(repo.getStarCount());
        repoResponse.setHasIssues(repo.getHasIssues());
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


}
