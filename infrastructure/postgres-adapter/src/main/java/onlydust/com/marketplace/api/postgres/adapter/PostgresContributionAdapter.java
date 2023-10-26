package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.view.ContributionView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubRepoMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresContributionAdapter implements ContributionStoragePort {

    private final ContributionRepository contributionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ContributionView> findContributionsForUser(Long contributorId,
                                                           ContributionView.Filters filters,
                                                           ContributionView.Sort sort,
                                                           SortDirection direction,
                                                           Integer page,
                                                           Integer pageSize) {
        final var contributionPage = contributionRepository.findContributionsForContributor(
                contributorId,
                filters.getProjects(),
                filters.getRepos(),
                isNull(filters.getTypes()) ? null :
                        filters.getTypes().stream().map(ContributionViewEntity.Type::of).toList(),
                isNull(filters.getStatuses()) ? null :
                        filters.getStatuses().stream().map(ContributionViewEntity.Status::of).toList(),
                PageRequest.of(page, pageSize, Sort.by(
                        direction == SortDirection.asc ? Sort.Direction.ASC : Sort.Direction.DESC,
                        sortBy(sort))));

        return Page.<ContributionView>builder()
                .content(contributionPage.getContent().stream().map(ContributionViewEntity::toView).toList())
                .totalItemNumber((int) contributionPage.getTotalElements())
                .totalPageNumber(contributionPage.getTotalPages())
                .build();
    }

    private String sortBy(ContributionView.Sort sort) {
        return switch (sort) {
            case CREATED_AT -> "createdAt";
            case PROJECT_REPO_NAME -> "project_name, repo_name";
            case GITHUB_NUMBER_TITLE -> "github_number, github_title";
        };
    }

    @Override
    public List<Project> listProjectsByContributor(Long contributorId, ContributionView.Filters filters) {
        return contributionRepository.listProjectsByContributor(contributorId, filters.getProjects(),
                        filters.getRepos()).stream()
                .map(ProjectMapper::mapShortProjectViewToProject)
                .toList();
    }

    @Override
    public List<GithubRepo> listReposByContributor(Long contributorId, ContributionView.Filters filters) {
        return contributionRepository.listReposByContributor(contributorId, filters.getProjects(),
                        filters.getRepos()).stream()
                .map(GithubRepoMapper::map)
                .toList();
    }
}
