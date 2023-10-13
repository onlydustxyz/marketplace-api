package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {

    private static final int TOP_CONTRIBUTOR_COUNT = 3;
    private final ProjectRepository projectRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final CustomRepoRepository customRepoRepository;
    private final CustomUserRepository customUserRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getById(UUID projectId) {
        final ProjectEntity projectEntity = projectRepository.getById(projectId);
        return getProjectDetails(projectEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailsView getBySlug(String slug) {
        final var projectEntity = projectRepository.findByKey(slug).orElseThrow();
        return getProjectDetails(projectEntity);
    }

    private ProjectDetailsView getProjectDetails(ProjectEntity projectEntity) {
        final var topContributors = customContributorRepository.findProjectTopContributors(projectEntity.getId(), TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(projectEntity.getId());
        final var repos = customRepoRepository.findProjectRepos(projectEntity.getId());
        final var leaders = customUserRepository.findProjectLeaders(projectEntity.getId());
        final var sponsors = customProjectRepository.getProjectSponsors(projectEntity.getId());
        return ProjectMapper.mapToProjectDetailsView(projectEntity, topContributors, contributorCount, repos, leaders, sponsors);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technology,
                                                                              List<String> sponsor, UUID userId,
                                                                              String search, ProjectCardView.SortBy sort) {
        return customProjectRepository.findByTechnologiesSponsorsOwnershipSearchSortBy(technology, sponsor,
                userId, search, sort);
    }

    @Override
    @Transactional
    public void createProject(UUID projectId,
                              String name,
                              String shortDescription,
                              String longDescription,
                              Boolean isLookingForContributors,
                              List<CreateProjectCommand.MoreInfo> moreInfos,
                              List<Long> githubRepoIds,
                              List<Long> githubUserIdsAsProjectLeads,
                              ProjectVisibility visibility,
                              String imageUrl) {
        final ProjectEntity projectEntity = ProjectEntity.builder()
                .id(projectId)
                .name(name)
                .shortDescription(shortDescription)
                .longDescription(longDescription)
                .hiring(isLookingForContributors)
                .logoUrl(imageUrl)
                .visibility(ProjectMapper.projectVisibilityToEntity(visibility))
                .rank(0)
                .build();
        moreInfos.stream().findFirst().ifPresent(moreInfo -> projectEntity.setTelegramLink(moreInfo.getUrl()));
        this.projectRepository.save(projectEntity);
    }

    @Override
    public List<Contributor> searchContributorsByLogin(UUID projectId, String login) {
        return customProjectRepository.findProjectContributorsByLogin(projectId, login).stream()
                .map(entity -> Contributor.builder()
                        .id(GithubUserIdentity.builder()
                                .githubUserId(entity.getGithubUserId())
                                .githubLogin(entity.getLogin())
                                .githubAvatarUrl(entity.getAvatarUrl())
                                .build())
                        .isRegistered(entity.getIsRegistered())
                        .build())
                .toList();
    }
}
