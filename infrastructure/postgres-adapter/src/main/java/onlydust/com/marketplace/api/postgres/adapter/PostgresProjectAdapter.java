package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectView;
import onlydust.com.marketplace.api.postgres.adapter.entity.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {

    private final ProjectRepository projectRepository;
    private final CustomProjectRepository customProjectRepository;

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technology,
                                                                             List<String> sponsor, UUID userId,
                                                                             String search, ProjectView.SortBy sort) {
        return customProjectRepository.findByTechnologiesSponsorsOwnershipSearchSortBy(technology, sponsor,
                userId, search, sort);
    }
}
