package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectContributorsMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class PostgresProjectAdapter implements ProjectStoragePort {

    private static final int TOP_CONTRIBUTOR_COUNT = 3;
    private final ProjectRepository projectRepository;
    private final ProjectIdRepository projectIdRepository;
    private final ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    private final ProjectRepoRepository projectRepoRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final CustomRepoRepository customRepoRepository;
    private final CustomUserRepository customUserRepository;
    private final CustomProjectListRepository customProjectListRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;

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
        final var topContributors = customContributorRepository.findProjectTopContributors(projectEntity.getId(),
                TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(projectEntity.getId());
        final var repos = customRepoRepository.findProjectRepos(projectEntity.getId());
        final var leaders = customUserRepository.findProjectLeaders(projectEntity.getId());
        final var sponsors = customProjectRepository.getProjectSponsors(projectEntity.getId());
        return ProjectMapper.mapToProjectDetailsView(projectEntity, topContributors, contributorCount, repos, leaders
                , sponsors);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies,
                                                                              List<String> sponsors, UUID userId,
                                                                              String search,
                                                                              ProjectCardView.SortBy sort,
                                                                              Boolean mine) {
        return customProjectListRepository.findByTechnologiesSponsorsUserIdSearchSortBy(technologies, sponsors,
                search, sort, userId, mine);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectCardView> findByTechnologiesSponsorsSearchSortBy(List<String> technologies,
                                                                        List<String> sponsors, String search,
                                                                        ProjectCardView.SortBy sort) {
        return customProjectListRepository.findByTechnologiesSponsorsSearchSortBy(technologies, sponsors, search, sort);
    }

    @Override
    @Transactional
    public void createProject(UUID projectId, String name, String shortDescription, String longDescription,
                              Boolean isLookingForContributors, List<CreateProjectCommand.MoreInfo> moreInfos,
                              List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeads,
                              ProjectVisibility visibility, String imageUrl) {
        final ProjectEntity projectEntity =
                ProjectEntity.builder().id(projectId).name(name).shortDescription(shortDescription).longDescription(longDescription).hiring(isLookingForContributors).logoUrl(imageUrl).visibility(ProjectMapper.projectVisibilityToEntity(visibility)).rank(0).build();
        moreInfos.stream().findFirst().ifPresent(moreInfo -> projectEntity.setTelegramLink(moreInfo.getUrl()));

        this.projectIdRepository.save(new ProjectIdEntity(projectId));
        this.projectRepository.save(projectEntity);

        this.projectRepoRepository.saveAll(githubRepoIds.stream().map(repoId -> new ProjectRepoEntity(projectId,
                repoId)).toList());

        this.projectLeaderInvitationRepository.saveAll(githubUserIdsAsProjectLeads.stream().map(githubUserId -> new ProjectLeaderInvitationEntity(UUID.randomUUID(), projectId, githubUserId)).toList());
    }

    @Override
    public List<Contributor> searchContributorsByLogin(UUID projectId, String login) {
        return customProjectRepository.findProjectContributorsByLogin(projectId, login).stream().map(entity -> Contributor.builder().id(GithubUserIdentity.builder().githubUserId(entity.getGithubUserId()).githubLogin(entity.getLogin()).githubAvatarUrl(entity.getAvatarUrl()).build()).isRegistered(entity.getIsRegistered()).build()).toList();
    }

    @Override
    public Page<ProjectContributorsLinkView> findContributors(UUID projectId,
                                                              ProjectContributorsLinkView.SortBy sortBy,
                                                              int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.countProjectContributorViewEntity(projectId);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, sortBy, pageIndex, pageSize)
                        .stream().map(ProjectContributorsMapper::mapToDomainWithoutProjectLeadData)
                        .toList();
        return Page.<ProjectContributorsLinkView>builder()
                .content(projectContributorsLinkViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    public Page<ProjectContributorsLinkView> findContributorsForProjectLead(UUID projectId,
                                                                            ProjectContributorsLinkView.SortBy sortBy
            , int pageIndex, int pageSize) {
        final Integer count = customContributorRepository.countProjectContributorViewEntity(projectId);
        final List<ProjectContributorsLinkView> projectContributorsLinkViews =
                customContributorRepository.getProjectContributorViewEntity(projectId, sortBy, pageIndex, pageSize)
                        .stream().map(ProjectContributorsMapper::mapToDomainWithProjectLeadData)
                        .toList();
        return Page.<ProjectContributorsLinkView>builder()
                .content(projectContributorsLinkViews)
                .totalItemNumber(count)
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .build();
    }

    @Override
    public List<UUID> getProjectLeadIds(UUID projectId) {
        return projectLeadViewRepository.findAllByProjectId(projectId).stream()
                .map(ProjectLeadViewEntity::getUserId)
                .toList();
    }
}
