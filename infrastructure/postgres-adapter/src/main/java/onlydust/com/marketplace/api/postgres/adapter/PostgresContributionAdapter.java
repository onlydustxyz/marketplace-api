package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionRewardQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ArchivedGithubContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CustomIgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubRepoMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.GithubPullRequest;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresContributionAdapter implements ContributionStoragePort {

    public static final long UNEXISTING_GITHUB_USER_ID = -1L;

    private final ContributionViewEntityRepository contributionViewEntityRepository;
    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;
    private final GithubRepoViewEntityRepository githubRepoViewEntityRepository;
    private final ContributionDetailsViewEntityRepository contributionDetailsViewEntityRepository;
    private final ContributionRewardViewEntityRepository contributionRewardViewEntityRepository;
    private final CustomContributorRepository customContributorRepository;
    private final CustomIgnoredContributionsRepository customIgnoredContributionsRepository;
    private final IgnoredContributionsRepository ignoredContributionsRepository;
    private final ProjectRepository projectRepository;
    private final ArchivedGithubContributionRepository archivedGithubContributionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ContributionView> findContributions(Optional<Long> callerGithubUserId,
                                                    ContributionView.Filters filters,
                                                    ContributionView.Sort sort,
                                                    SortDirection direction,
                                                    Integer page,
                                                    Integer pageSize) {
        final var format = new SimpleDateFormat("yyyy-MM-dd");
        final var contributionPage = contributionViewEntityRepository.findContributions(
                callerGithubUserId.orElse(UNEXISTING_GITHUB_USER_ID),
                filters.getContributors(),
                filters.getProjects().stream().map(ProjectId::value).toList(),
                filters.getRepos(),
                filters.getTypes().stream().map(Enum::name).toList(),
                filters.getStatuses().stream().map(Enum::name).toList(),
                filters.getLanguages().toArray(UUID[]::new),
                filters.getEcosystems().toArray(UUID[]::new),
                Boolean.TRUE.equals(filters.getIncludePrivateProjects()),
                isNull(filters.getFrom()) ? null : format.format(filters.getFrom()),
                isNull(filters.getTo()) ? null : format.format(filters.getTo()),
                PageRequest.of(page, pageSize, sortBy(sort, direction == SortDirection.asc ? Sort.Direction.ASC :
                        Sort.Direction.DESC)));

        return Page.<ContributionView>builder()
                .content(contributionPage.getContent().stream().map(ContributionViewEntity::toView).toList())
                .totalItemNumber((int) contributionPage.getTotalElements())
                .totalPageNumber(contributionPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ContributionDetailsView findContributionById(ProjectId projectId, String contributionId) {
        final var contribution = contributionDetailsViewEntityRepository.findContributionById(projectId.value(), contributionId)
                .orElseThrow(() -> OnlyDustException.notFound("contribution not found"));

        final var rewards = contributionRewardViewEntityRepository.listByContributionId(projectId.value(),
                contributionId);
        return contribution.toView()
                .withRewards(rewards.stream().map(ContributionRewardQueryEntity::toView).toList());
    }

    private Sort sortBy(ContributionView.Sort sort, Sort.Direction direction) {
        return switch (sort) {
            case CREATED_AT -> Sort.by(direction, "created_at");
            case LAST_UPDATED_AT -> JpaSort.unsafe(direction, "coalesce(c.completed_at, c.created_at)");
            case PROJECT_REPO_NAME -> Sort.by(direction, "project_name", "repo_name");
            case GITHUB_NUMBER_TITLE -> Sort.by(direction, "github_number", "github_title");
            case CONTRIBUTOR_LOGIN -> Sort.by(direction, "contributor_login");
            case LINKS_COUNT -> JpaSort.unsafe(direction, "COALESCE(jsonb_array_length(COALESCE(closing_issues.links," +
                                                          "closing_pull_requests.links, reviewed_pull_requests.links)" +
                                                          "), 0)");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> listProjectsByContributor(Long contributorId, ContributionView.Filters filters) {
        return shortProjectViewEntityRepository.listProjectsByContributor(contributorId, filters.getProjects().stream().map(ProjectId::value).toList(),
                        filters.getRepos()).stream()
                .map(ProjectMapper::mapShortProjectViewToProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GithubRepo> listReposByContributor(Long contributorId, ContributionView.Filters filters) {
        return githubRepoViewEntityRepository.listReposByContributor(contributorId, filters.getProjects().stream().map(ProjectId::value).toList(),
                        filters.getRepos()).stream()
                .map(GithubRepoMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getContributorId(String contributionId) {
        return customContributorRepository.getContributionContributorId(contributionId)
                .orElseThrow(() -> OnlyDustException.notFound("contribution not found"));
    }

    @Override
    @Transactional
    public void ignoreContributions(ProjectId projectId, List<String> contributionIds) {
        customIgnoredContributionsRepository.saveAllAndFlush(contributionIds.stream().map(contributionId ->
                CustomIgnoredContributionEntity.builder()
                        .id(CustomIgnoredContributionEntity.Id.builder()
                                .projectId(projectId.value())
                                .contributionId(contributionId)
                                .build())
                        .ignored(true)
                        .build()
        ).toList());

        ignoredContributionsRepository.saveAllAndFlush(contributionIds.stream().map(contributionId ->
                IgnoredContributionEntity.builder()
                        .id(IgnoredContributionEntity.Id.builder()
                                .projectId(projectId.value())
                                .contributionId(contributionId)
                                .build())
                        .build()
        ).toList());
    }

    @Override
    @Transactional
    public void unignoreContributions(ProjectId projectId, List<String> contributionIds) {
        customIgnoredContributionsRepository.saveAllAndFlush(contributionIds.stream().map(contributionId ->
                CustomIgnoredContributionEntity.builder()
                        .id(CustomIgnoredContributionEntity.Id.builder()
                                .projectId(projectId.value())
                                .contributionId(contributionId)
                                .build())
                        .ignored(false)
                        .build()
        ).toList());

        ignoredContributionsRepository.deleteAll(contributionIds.stream().map(contributionId ->
                IgnoredContributionEntity.builder()
                        .id(IgnoredContributionEntity.Id.builder()
                                .projectId(projectId.value())
                                .contributionId(contributionId)
                                .build())
                        .build()
        ).toList());

    }

    @Override
    @Transactional
    public void refreshIgnoredContributions(ProjectId projectId) {
        final var repoIds = projectRepository.findById(projectId.value())
                .orElseThrow(() -> OnlyDustException.notFound("project %s not found".formatted(projectId)))
                .getRepos().stream()
                .map(ProjectRepoEntity::getRepoId)
                .toList();

        ignoredContributionsRepository.deleteContributionsThatAreNotPartOfTheProjectAnymore(projectId.value());
        customIgnoredContributionsRepository.deleteContributionsThatAreNotPartOfTheProjectAnymore(projectId.value());
        refreshIgnoredContributions(repoIds);
    }

    @Override
    @Transactional
    public void refreshIgnoredContributions(List<Long> repoIds) {
        ignoredContributionsRepository.addMissingContributions(repoIds);
        ignoredContributionsRepository.deleteContributionsThatShouldNotBeIgnored(repoIds);
    }


    @Override
    @Transactional
    public void archiveIssue(GithubIssue.Id id, Boolean archived) {
        archiveGithubContribution(id.value(), archived);
    }

    @Override
    @Transactional
    public void archivePullRequest(GithubPullRequest.Id id, Boolean archived) {
        archiveGithubContribution(id.value(), archived);
    }

    private void archiveGithubContribution(Long id, Boolean archived) {
        final Optional<ArchivedGithubContributionEntity> isAlreadyArchived = archivedGithubContributionRepository.findById(id);
        if (isAlreadyArchived.isPresent() && !archived) {
            archivedGithubContributionRepository.delete(isAlreadyArchived.get());
        }
        if (isAlreadyArchived.isEmpty() && archived) {
            archivedGithubContributionRepository.save(new ArchivedGithubContributionEntity(id));
        }
    }
}
