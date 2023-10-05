package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectLeadView;
import onlydust.com.marketplace.api.domain.view.SponsorView;

import java.util.*;

public interface ProjectMapper {

    static ShortProjectResponse projectToResponse(final Project project) {
        final ShortProjectResponse projectResponse = new ShortProjectResponse();
        projectResponse.setId(project.getId());
        projectResponse.setName(project.getName());
        projectResponse.setLogoUrl(project.getLogoUrl());
        projectResponse.setShortDescription(project.getShortDescription());
        projectResponse.setPrettyId(project.getSlug());
        projectResponse.setVisibility(ShortProjectResponse.VisibilityEnum.PUBLIC);
        return projectResponse;
    }

    static ProjectListResponse projectViewsToProjectListResponse(final Page<ProjectCardView> projectViewPage) {
        final ProjectListResponse projectListResponse = new ProjectListResponse();
        final List<ProjectListItemResponse> projectListItemResponses = new ArrayList<>();
        final Set<String> sponsorsNames = new HashSet<>();
        final Set<String> technologies = new HashSet<>();
        for (ProjectCardView projectCardView : projectViewPage.getContent()) {
            final ProjectListItemResponse projectListItemResponse = mapProject(projectCardView);
            mapProjectLead(projectCardView, projectListItemResponse);
            mapSponsors(projectCardView, projectListItemResponse, sponsorsNames);
            projectListItemResponse.setTechnologies(projectCardView.getTechnologies());
            technologies.addAll(projectCardView.getTechnologies().keySet());
            projectListItemResponses.add(projectListItemResponse);
        }
        projectListResponse.setProjects(projectListItemResponses);
        projectListResponse.setTechnologies(technologies.stream().toList());
        projectListResponse.setSponsors(sponsorsNames.stream().toList());
        return projectListResponse;
    }

    private static ProjectListItemResponse mapProject(final ProjectCardView projectCardView) {
        final ProjectListItemResponse projectListItemResponse = new ProjectListItemResponse();
        projectListItemResponse.setId(projectCardView.getId());
        projectListItemResponse.setName(projectCardView.getName());
        projectListItemResponse.setLogoUrl(projectCardView.getLogoUrl());
        projectListItemResponse.setPrettyId(projectCardView.getSlug());
        projectListItemResponse.setHiring(projectCardView.getHiring());
        projectListItemResponse.setShortDescription(projectCardView.getShortDescription());
        projectListItemResponse.setContributorCount(projectCardView.getContributorCount());
        projectListItemResponse.setRepoCount(projectCardView.getRepoCount());
        return projectListItemResponse;
    }

    private static void mapSponsors(final ProjectCardView projectCardView,
                                    final ProjectListItemResponse projectListItemResponse,
                                    final Set<String> sponsorsNames) {
        for (SponsorView sponsorView : projectCardView.getSponsors()) {
            final SponsorResponse sponsorResponse = new SponsorResponse();
            sponsorResponse.setId(sponsorView.getId());
            sponsorResponse.setName(sponsorView.getName());
            sponsorsNames.add(sponsorView.getName());
            sponsorResponse.setLogoUrl(sponsorView.getLogoUrl());
            projectListItemResponse.addSponsorsItem(sponsorResponse);
        }
    }

    private static void mapProjectLead(final ProjectCardView projectCardView,
                                       final ProjectListItemResponse projectListItemResponse) {
        for (ProjectLeadView projectLeadView : projectCardView.getProjectLeadViews()) {
            final RegisteredUserMinimalistResponse registeredUserMinimalistResponse =
                    new RegisteredUserMinimalistResponse();
            registeredUserMinimalistResponse.setId(projectCardView.getId());
            registeredUserMinimalistResponse.setAvatarUrl(projectLeadView.getAvatarUrl());
            registeredUserMinimalistResponse.setLogin(projectLeadView.getLogin());
            projectListItemResponse.addLeadersItem(registeredUserMinimalistResponse);
        }
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
