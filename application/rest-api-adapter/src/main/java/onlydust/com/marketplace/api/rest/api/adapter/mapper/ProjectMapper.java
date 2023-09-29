package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.*;

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

    static ProjectListResponse projectViewsToProjectListResponse(final Page<ProjectView> projectViewPage) {
        final ProjectListResponse projectListResponse = new ProjectListResponse();
        final List<ProjectListItemResponse> projectListItemResponses = new ArrayList<>();
        final Set<String> sponsorsNames = new HashSet<>();
        final Set<String> technologies = new HashSet<>();
        for (ProjectView projectView : projectViewPage.getContent()) {
            final ProjectListItemResponse projectListItemResponse = mapProject(projectView);
            mapProjectLead(projectView, projectListItemResponse);
            mapSponsors(projectView, projectListItemResponse, sponsorsNames);
            projectListItemResponse.setTechnologies(repositoriesToTechnologies(projectView.getRepositories().stream().toList(), technologies));
            projectListItemResponses.add(projectListItemResponse);
        }
        projectListResponse.setProjects(projectListItemResponses);
        projectListResponse.setTechnologies(technologies.stream().toList());
        projectListResponse.setSponsors(sponsorsNames.stream().toList());
        return projectListResponse;
    }

    private static ProjectListItemResponse mapProject(final ProjectView projectView) {
        final ProjectListItemResponse projectListItemResponse = new ProjectListItemResponse();
        projectListItemResponse.setId(projectView.getId());
        projectListItemResponse.setName(projectView.getName());
        projectListItemResponse.setLogoUrl(projectView.getLogoUrl());
        projectListItemResponse.setPrettyId(projectView.getSlug());
        projectListItemResponse.setHiring(projectView.getHiring());
        projectListItemResponse.setShortDescription(projectView.getShortDescription());
        projectListItemResponse.setContributorCount(projectView.getContributorCount());
        projectListItemResponse.setRepoCount(projectView.getRepositoryCount());
        return projectListItemResponse;
    }

    private static void mapSponsors(final ProjectView projectView,
                                    final ProjectListItemResponse projectListItemResponse,
                                    final Set<String> sponsorsNames) {
        for (SponsorView sponsorView : projectView.getSponsors()) {
            final SponsorResponse sponsorResponse = new SponsorResponse();
            sponsorResponse.setId(sponsorView.getId());
            sponsorResponse.setName(sponsorView.getName());
            sponsorsNames.add(sponsorView.getName());
            sponsorResponse.setLogoUrl(sponsorView.getLogoUrl());
            projectListItemResponse.addSponsorsItem(sponsorResponse);
        }
    }

    private static void mapProjectLead(final ProjectView projectView,
                                       final ProjectListItemResponse projectListItemResponse) {
        for (ProjectLeadView projectLeadView : projectView.getProjectLeadViews()) {
            final RegisteredUserMinimalistResponse registeredUserMinimalistResponse =
                    new RegisteredUserMinimalistResponse();
            registeredUserMinimalistResponse.setId(projectView.getId());
            registeredUserMinimalistResponse.setAvatarUrl(projectLeadView.getAvatarUrl());
            registeredUserMinimalistResponse.setLogin(projectLeadView.getLogin());
            projectListItemResponse.addLeadersItem(registeredUserMinimalistResponse);
        }
    }

    static Map<String, Integer> repositoriesToTechnologies(final List<RepositoryView> repositoryViews,
                                                           final Set<String> technologiesNames) {
        final Map<String, Integer> technologies = new HashMap<>();
        for (RepositoryView repositoryView : repositoryViews) {
            repositoryView.getTechnologies().forEach((key, value) -> {
                if (technologies.containsKey(key)) {
                    technologies.replace(key, technologies.get(key) + value);
                } else {
                    technologies.put(key, value);
                    technologiesNames.add(key);
                }
            });
        }
        return technologies;
    }

    static ProjectView.SortBy mapSortByParameter(final String sort) {
        if (Objects.nonNull(sort)) {
            if (sort.equals("RANK")) {
                return ProjectView.SortBy.RANK;
            }
            if (sort.equals("NAME")) {
                return ProjectView.SortBy.NAME;
            }
            if (sort.equals("REPO_COUNT")) {
                return ProjectView.SortBy.REPOS_COUNT;
            }
            if (sort.equals("CONTRIBUTOR_COUNT")) {
                return ProjectView.SortBy.CONTRIBUTORS_COUNT;
            }
        }
        return null;
    }
}
