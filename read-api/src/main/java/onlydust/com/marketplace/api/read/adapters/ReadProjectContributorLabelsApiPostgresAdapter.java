package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectContributorLabelsApi;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelListResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectContributorLabelReadEntity;
import onlydust.com.marketplace.api.read.repositories.ProjectContributorLabelReadRepository;
import onlydust.com.marketplace.kernel.model.OrSlug;
import onlydust.com.marketplace.kernel.model.ProjectId;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadProjectContributorLabelsApiPostgresAdapter implements ReadProjectContributorLabelsApi {
    private final ProjectContributorLabelReadRepository projectContributorLabelReadRepository;

    @Override
    public ResponseEntity<ProjectContributorLabelListResponse> getProjectContributorLabels(String projectIdOrSlugStr) {
        final var projectIdOrSlug = OrSlug.of(projectIdOrSlugStr, ProjectId::of);
        final var labels = projectIdOrSlug.uuid().map(projectContributorLabelReadRepository::findAllByProjectId)
                .or(() -> projectIdOrSlug.slug().map(projectContributorLabelReadRepository::findAllByProjectSlug))
                .orElseThrow(() -> badRequest("Invalid project id or slug"));

        return ResponseEntity.ok(new ProjectContributorLabelListResponse().labels(labels.stream()
                .map(ProjectContributorLabelReadEntity::toDto)
                .toList()));
    }
}
