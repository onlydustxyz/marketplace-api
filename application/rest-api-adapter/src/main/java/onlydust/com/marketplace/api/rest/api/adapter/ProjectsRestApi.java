package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.ProjectsApi;
import onlydust.com.marketplace.api.contract.model.CreateProjectRequest;
import onlydust.com.marketplace.api.contract.model.CreateProjectResponse;
import onlydust.com.marketplace.api.contract.model.ProjectListResponse;
import onlydust.com.marketplace.api.contract.model.ProjectResponse;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.*;

@RestController
@Tags(@Tag(name = "Projects"))
@AllArgsConstructor
@Slf4j
public class ProjectsRestApi implements ProjectsApi {

    private final ProjectFacadePort projectFacadePort;

    @Override
    public ResponseEntity<ProjectResponse> getProject(final UUID projectId) {
        final var project = projectFacadePort.getById(projectId);
        final var projectResponse = mapProjectDetails(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectResponse> getProjectBySlug(final String slug) {
        final var project = projectFacadePort.getBySlug(slug);
        final var projectResponse = mapProjectDetails(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectListResponse> getProjects(final String sort, final List<String> technology,
                                                           final List<String> sponsor, final String ownership,
                                                           final String search) {
        final Page<ProjectCardView> projectViewPage =
                projectFacadePort.getByTechnologiesSponsorsUserIdSearchSortBy(technology, sponsor, null,
                        search, mapSortByParameter(sort));
        return ResponseEntity.ok(mapProjectCards(projectViewPage));
    }

    @Override
    public ResponseEntity<CreateProjectResponse> createProject(CreateProjectRequest createProjectRequest) {
        final UUID projectId = projectFacadePort.createProject(
                CreateProjectCommand.builder()
                        .name(createProjectRequest.getName())
                        .shortDescription(createProjectRequest.getShortDescription())
                        .longDescription(createProjectRequest.getLongDescription())
                        .githubUserIdsAsProjectLeads(createProjectRequest.getInviteGithubUserIdsAsProjectLeads())
                        .githubRepoIds(createProjectRequest.getGithubRepoIds())
                        .isLookingForContributors(createProjectRequest.getIsLookingForContributors())
                        .moreInfos(createProjectRequest.getMoreInfo().stream()
                                .map(moreInfo -> CreateProjectCommand.MoreInfo.builder()
                                        .url(moreInfo.getUrl())
                                        .value(moreInfo.getValue())
                                        .build())
                                .toList())
                        .build()
        );
        final CreateProjectResponse createProjectResponse = new CreateProjectResponse();
        createProjectResponse.setProjectId(projectId);
        return ResponseEntity.ok(createProjectResponse);
    }


}
