package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.*;

import java.util.*;
import java.util.stream.Collectors;

public interface ProjectMapper {

    static ProjectResponse mapProjectDetails(final ProjectDetailsView project) {
        final ProjectResponse projectListItemResponse = mapProjectDetailsMetadata(project);

        for (ProjectLeaderLinkView leader : project.getLeaders()) {
            projectListItemResponse.addLeadersItem(mapProjectLead(leader));
        }
        for (SponsorView sponsor : project.getSponsors()) {
            projectListItemResponse.addSponsorsItem(mapSponsor(sponsor));
        }
        projectListItemResponse.setTechnologies(project.getTechnologies());

        return projectListItemResponse;
    }

    private static ProjectResponse mapProjectDetailsMetadata(final ProjectDetailsView projectDetailsView) {
        final var project = new ProjectResponse();
        project.setId(projectDetailsView.getId());
        project.setName(projectDetailsView.getName());
        project.setLogoUrl(projectDetailsView.getLogoUrl());
        project.setSlug(projectDetailsView.getSlug());
        project.setHiring(projectDetailsView.getHiring());
        project.setShortDescription(projectDetailsView.getShortDescription());
        project.setContributorCount(projectDetailsView.getContributorCount());
        project.setVisibility(mapProjectVisibility(projectDetailsView.getVisibility()));
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
        projectListResponse.setTechnologies(technologies.stream().toList());
        projectListResponse.setSponsors(sponsorsNames.stream().toList());
        return projectListResponse;
    }

    private static ProjectListItemResponse mapProjectCard(ProjectCardView projectCardView, Set<String> sponsorsNames, Set<String> technologies) {
        final ProjectListItemResponse projectListItemResponse = mapProjectCardMetadata(projectCardView);

        for (ProjectLeaderLinkView leader : projectCardView.getLeaders()) {
            projectListItemResponse.addLeadersItem(mapProjectLead(leader));
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
        return project;
    }

    private static SponsorResponse mapSponsor(final SponsorView sponsor) {
        final SponsorResponse sponsorResponse = new SponsorResponse();
        sponsorResponse.setId(sponsor.getId());
        sponsorResponse.setName(sponsor.getName());
        sponsorResponse.setLogoUrl(sponsor.getLogoUrl());
        return sponsorResponse;
    }

    private static RegisteredUserMinimalistResponse mapProjectLead(final ProjectLeaderLinkView leader) {
        final var userLink = new RegisteredUserMinimalistResponse();
        userLink.setId(leader.getId());
        userLink.setAvatarUrl(leader.getAvatarUrl());
        userLink.setLogin(leader.getLogin());
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
