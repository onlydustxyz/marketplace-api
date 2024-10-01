package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.ProjectContributorLabelsApi;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelRequest;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "ProjectContributorLabels"))
@AllArgsConstructor
@Slf4j
@Profile("api")
public class ProjectContributorLabelsRestApi implements ProjectContributorLabelsApi {

    @Override
    public ResponseEntity<ProjectContributorLabelResponse> createProjectContributorLabel(UUID projectId,
                                                                                         ProjectContributorLabelRequest projectContributorLabelRequest) {
        // TODO: Implement this method
        return ProjectContributorLabelsApi.super.createProjectContributorLabel(projectId, projectContributorLabelRequest);
    }

    @Override
    public ResponseEntity<Void> deleteProjectContributorLabel(UUID projectId, UUID labelId) {
        // TODO: Implement this method
        return ProjectContributorLabelsApi.super.deleteProjectContributorLabel(projectId, labelId);
    }

    @Override
    public ResponseEntity<Void> updateProjectContributorLabel(UUID projectId, UUID labelId, ProjectContributorLabelRequest projectContributorLabelRequest) {
        // TODO: Implement this method
        return ProjectContributorLabelsApi.super.updateProjectContributorLabel(projectId, labelId, projectContributorLabelRequest);
    }
}
