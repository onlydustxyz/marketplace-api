package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectContributorLabelsApi;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelListResponse;
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

    @Override
    public ResponseEntity<ProjectContributorLabelListResponse> getProjectContributorLabels(UUID projectId) {
        // TODO: Implement this method
        return ReadProjectContributorLabelsApi.super.getProjectContributorLabels(projectId);
    }
}
