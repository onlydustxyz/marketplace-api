package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ProjectsApi;
import onlydust.com.marketplace.api.contract.model.ShortProjectResponse;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "Projects"))
@AllArgsConstructor
public class ProjectRestApi implements ProjectsApi {

    private final ProjectFacadePort projectFacadePort;

    @Override
    public ResponseEntity<ShortProjectResponse> getProject(UUID projectId) {
        final Project project = projectFacadePort.getById(projectId);
        final ShortProjectResponse projectResponse = projectToResponse(project);
        return ResponseEntity.ok(projectResponse);
    }

    @Override
    public ResponseEntity<ShortProjectResponse> getProjectBySlug(String slug) {
        final Project project = projectFacadePort.getBySlug(slug);
        final ShortProjectResponse projectResponse = projectToResponse(project);
        return ResponseEntity.ok(projectResponse);
    }

    private static ShortProjectResponse projectToResponse(Project project) {
        final ShortProjectResponse projectResponse = new ShortProjectResponse();
        projectResponse.setId(project.getId());
        projectResponse.setName(project.getName());
        projectResponse.setLogoUrl(project.getLogoUrl());
        projectResponse.setShortDescription(project.getShortDescription());
        projectResponse.setPrettyId(project.getSlug());
        projectResponse.setVisibility(ShortProjectResponse.VisibilityEnum.PUBLIC);
        return projectResponse;
    }
}
