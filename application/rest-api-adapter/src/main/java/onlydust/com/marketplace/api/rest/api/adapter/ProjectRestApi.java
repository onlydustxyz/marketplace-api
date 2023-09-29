package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ProjectsApi;
import onlydust.com.marketplace.api.contract.model.ProjectListResponse;
import onlydust.com.marketplace.api.contract.model.ShortProjectResponse;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.*;

@RestController
@Tags(@Tag(name = "Projects"))
@AllArgsConstructor
public class ProjectRestApi implements ProjectsApi {

    private final ProjectFacadePort projectFacadePort;

    @Override
    public ResponseEntity<ShortProjectResponse> getProject(final UUID projectId) {
        final Project project = projectFacadePort.getById(projectId);
        final ShortProjectResponse projectResponse = projectToResponse(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ShortProjectResponse> getProjectBySlug(final String slug) {
        final Project project = projectFacadePort.getBySlug(slug);
        final ShortProjectResponse projectResponse = projectToResponse(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectListResponse> getProjects(final String sort, final List<String> technology,
                                                           final List<String> sponsor, final String ownership,
                                                           final String search) {
        final Page<ProjectView> projectViewPage =
                projectFacadePort.getByTechnologiesSponsorsUserIdSearchSortBy(technology, sponsor, null,
                        search, mapSortByParameter(sort));
        return ResponseEntity.ok(projectViewsToProjectListResponse(projectViewPage));
    }


}
