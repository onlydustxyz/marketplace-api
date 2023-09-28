package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public Project getById(UUID projectId) {
        final ProjectEntity projectEntity = projectRepository.getById(projectId);
        return Project.builder()
                .id(projectEntity.getId())
                .hiring(projectEntity.getHiring())
                .logoUrl(projectEntity.getLogoUrl())
                .longDescription(projectEntity.getLongDescription())
                .shortDescription(projectEntity.getShortDescription())
                .slug(projectEntity.getKey())
                .name(projectEntity.getName())
                .build();
    }

    @Override
    @Transactional
    public Project getBySlug(String slug) {
        final ProjectEntity projectEntity = projectRepository.findByKey(slug).orElseThrow();
        return Project.builder()
                .id(projectEntity.getId())
                .hiring(projectEntity.getHiring())
                .logoUrl(projectEntity.getLogoUrl())
                .longDescription(projectEntity.getLongDescription())
                .shortDescription(projectEntity.getShortDescription())
                .slug(projectEntity.getKey())
                .name(projectEntity.getName())
                .build();
    }
}
