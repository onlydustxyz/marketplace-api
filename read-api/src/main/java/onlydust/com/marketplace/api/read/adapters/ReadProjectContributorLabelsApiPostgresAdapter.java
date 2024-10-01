package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectContributorLabelsApi;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelListResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectContributorLabelReadEntity;
import onlydust.com.marketplace.api.read.repositories.ProjectContributorLabelReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadProjectContributorLabelsApiPostgresAdapter implements ReadProjectContributorLabelsApi {
    private final ProjectContributorLabelReadRepository projectContributorLabelReadRepository;

    @Override
    public ResponseEntity<ProjectContributorLabelListResponse> getProjectContributorLabels(UUID projectId) {
        final var labels = projectContributorLabelReadRepository.findAllByProjectId(projectId);
        return ResponseEntity.ok(new ProjectContributorLabelListResponse().labels(labels.stream()
                .map(ProjectContributorLabelReadEntity::toDto)
                .toList()));
    }
}
