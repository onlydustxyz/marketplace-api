package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.port.output.BackofficeStoragePort;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
public class PostgresBackofficeAdapter implements BackofficeStoragePort {
    private final ProjectRepository projectRepository;

    @Override
    public Page<ProjectView> searchProjects(int pageIndex, int pageSize, String search) {
        final var pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("name"));

        final var page = search != null
                ? projectRepository.findAllByNameContainingIgnoreCase(search, pageRequest)
                : projectRepository.findAll(pageRequest);

        return Page.<ProjectView>builder()
                .content(page.getContent().stream().map(ProjectEntity::toBoView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
