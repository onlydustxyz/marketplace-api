package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.data.processing.contract.api.ProjectsApi;
import io.symeo.monolithic.backend.data.processing.contract.api.model.ProjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "Projects"))
@AllArgsConstructor
public class ProjectRestApi implements ProjectsApi {

    @Override
    public ResponseEntity<ProjectResponse> getProject(UUID projectId) {
        return ProjectsApi.super.getProject(projectId);
    }
}
