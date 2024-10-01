package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectContributorLabelEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectContributorLabelRepository;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.output.ProjectContributorLabelStoragePort;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class PostgresProjectContributorLabelAdapter implements ProjectContributorLabelStoragePort {
    private final ProjectContributorLabelRepository projectContributorLabelRepository;

    @Override
    @Transactional
    public void save(ProjectContributorLabel projectContributorLabel) {
        projectContributorLabelRepository.save(ProjectContributorLabelEntity.fromDomain(projectContributorLabel));
    }
}
