package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ContributorProjectContributorLabelEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectContributorLabelEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributorProjectContributorLabelRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectContributorLabelRepository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.output.ProjectContributorLabelStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresProjectContributorLabelAdapter implements ProjectContributorLabelStoragePort {
    private final ProjectContributorLabelRepository projectContributorLabelRepository;
    private final ContributorProjectContributorLabelRepository contributorProjectContributorLabelRepository;

    @Override
    @Transactional
    public void save(@NonNull ProjectContributorLabel projectContributorLabel) {
        projectContributorLabelRepository.save(ProjectContributorLabelEntity.fromDomain(projectContributorLabel));
    }

    @Override
    public void delete(ProjectContributorLabel.@NonNull Id labelId) {
        projectContributorLabelRepository.deleteById(labelId.value());
    }

    @Override
    public Optional<ProjectContributorLabel> get(ProjectContributorLabel.@NonNull Id labelId) {
        return projectContributorLabelRepository.findById(labelId.value())
                .map(ProjectContributorLabelEntity::toDomain);
    }

    @Override
    public void deleteLabelsOfContributor(@NonNull ProjectId projectId, Long contributorId) {
        contributorProjectContributorLabelRepository.deleteByProjectIdAndContributorId(projectId.value(), contributorId);
    }

    @Override
    public void saveLabelOfContributor(ProjectContributorLabel.Id labelId, Long contributorId) {
        contributorProjectContributorLabelRepository.save(new ContributorProjectContributorLabelEntity(labelId.value(), contributorId, null));
    }

}
