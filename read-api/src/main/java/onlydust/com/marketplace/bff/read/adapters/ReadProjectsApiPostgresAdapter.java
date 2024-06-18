package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectMoreInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoStatsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributionViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomContributorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectLeadViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.bff.read.entities.github.GithubIssueReadEntity;
import onlydust.com.marketplace.bff.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.bff.read.entities.project.ProjectPageItemFiltersQueryEntity;
import onlydust.com.marketplace.bff.read.entities.project.ProjectPageItemQueryEntity;
import onlydust.com.marketplace.bff.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.bff.read.mapper.ProjectMapper;
import onlydust.com.marketplace.bff.read.mapper.RewardsMapper;
import onlydust.com.marketplace.bff.read.mapper.SponsorMapper;
import onlydust.com.marketplace.bff.read.mapper.UserMapper;
import onlydust.com.marketplace.bff.read.repositories.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.mapProjectVisibility;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.mapRewardSettings;
import static onlydust.com.marketplace.bff.read.entities.project.ProjectPageItemQueryEntity.*;
import static onlydust.com.marketplace.bff.read.mapper.ProjectMapper.mapSortByParameter;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadProjectsApiPostgresAdapter implements ReadProjectsApi {
    private static final int TOP_CONTRIBUTOR_COUNT = 3;

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final GithubIssueReadRepository githubIssueReadRepository;
    private final ProjectReadRepository projectReadRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final ApplicationRepository applicationRepository;
    private final ContributionViewEntityRepository contributionViewEntityRepository;
    private final ProjectsPageRepository projectsPageRepository;
    private final ProjectsPageFiltersRepository projectsPageFiltersRepository;
    private final RewardDetailsReadRepository rewardDetailsReadRepository;
    private final BudgetStatsReadRepository budgetStatsReadRepository;
    private final ProjectContributorQueryRepository projectContributorQueryRepository;

    @Override
    public ResponseEntity<ProjectPageResponse> getProjects(final Integer pageIndex,
                                                           final Integer pageSize,
                                                           final Boolean mine,
                                                           final String search,
                                                           final List<ProjectTag> tags,
                                                           final List<String> ecosystemSlugs,
                                                           final List<String> languageSlugs,
                                                           final List<String> categorySlugs,
                                                           final Boolean hasGoodFirstIssues,
                                                           final String sort
    ) {
        final Optional<User> user = authenticatedAppUserService.tryGetAuthenticatedUser();
        final ProjectMapper.SortBy sortBy = mapSortByParameter(sort);

        final String ecosystemsJsonPath = getEcosystemsJsonPath(ecosystemSlugs);
        final String tagsJsonPath = getTagsJsonPath(isNull(tags) ? null : tags.stream().map(Enum::name).toList());
        final String languagesJsonPath = getLanguagesJsonPath(languageSlugs);

        return ResponseEntity.ok(user.map(u -> getProjectsForAuthenticatedUser(u.getId(), mine, search, ecosystemsJsonPath, tagsJsonPath, languagesJsonPath,
                        categorySlugs, hasGoodFirstIssues, sanitizePageIndex(pageIndex), sanitizePageSize(pageSize), sortBy))
                .orElseGet(() -> getProjectsForAnonymousUser(search, ecosystemsJsonPath, tagsJsonPath, languagesJsonPath, categorySlugs, hasGoodFirstIssues,
                        sanitizePageIndex(pageIndex), sanitizePageSize(pageSize), sortBy)));
    }

    private ProjectPageResponse getProjectsForAuthenticatedUser(UUID userId,
                                                                Boolean mine,
                                                                String search,
                                                                String ecosystemsJsonPath, String tagsJsonPath, String languagesJsonPath,
                                                                List<String> categorySlugs, Boolean hasGoodFirstIssues,
                                                                Integer pageIndex, Integer pageSize, ProjectMapper.SortBy sortBy) {
        final Long count = projectsPageRepository.countProjectsForUserId(userId, mine,
                search,
                tagsJsonPath,
                ecosystemsJsonPath,
                languagesJsonPath,
                categorySlugs,
                hasGoodFirstIssues);
        final var projects = projectsPageRepository.findProjectsForUserId(userId, mine,
                search,
                tagsJsonPath,
                ecosystemsJsonPath,
                languagesJsonPath,
                categorySlugs,
                hasGoodFirstIssues,
                isNull(sortBy) ? ProjectMapper.SortBy.NAME.name() : sortBy.name(),
                PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex),
                pageSize);
        final var filters = projectsPageFiltersRepository.findFiltersForUser(userId, mine);
        final var totalNumberOfPage = calculateTotalNumberOfPage(pageSize, count.intValue());

        return toProjectPage(userId, pageIndex, projects, filters, totalNumberOfPage, count);
    }

    private ProjectPageResponse getProjectsForAnonymousUser(String search,
                                                            String ecosystemsJsonPath, String tagsJsonPath, String languagesJsonPath,
                                                            List<String> categorySlugs, Boolean hasGoodFirstIssues,
                                                            Integer pageIndex, Integer pageSize, ProjectMapper.SortBy sortBy) {
        final Long count = projectsPageRepository.countProjectsForAnonymousUser(
                search,
                tagsJsonPath,
                ecosystemsJsonPath,
                languagesJsonPath,
                categorySlugs,
                hasGoodFirstIssues);
        final var projects = projectsPageRepository.findProjectsForAnonymousUser(
                search,
                tagsJsonPath,
                ecosystemsJsonPath,
                languagesJsonPath,
                categorySlugs,
                hasGoodFirstIssues,
                isNull(sortBy) ? ProjectMapper.SortBy.NAME.name() : sortBy.name(),
                PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex),
                pageSize);
        final var filters = projectsPageFiltersRepository.findFiltersForAnonymousUser();
        final var totalNumberOfPage = calculateTotalNumberOfPage(pageSize, count.intValue());

        return toProjectPage(null, pageIndex, projects, filters, totalNumberOfPage, count);
    }

    private static ProjectPageResponse toProjectPage(UUID userId, Integer pageIndex, List<ProjectPageItemQueryEntity> projects,
                                                     List<ProjectPageItemFiltersQueryEntity> filters, int totalNumberOfPage, Long count) {
        return new ProjectPageResponse()
                .projects(projects.stream().map(p -> p.toDto(userId)).toList())
                .languages(filters.stream().flatMap(e -> e.languages().stream().map(LanguageReadEntity::toDto)).collect(toSet()).stream().toList())
                .ecosystems(filters.stream().flatMap(e1 -> e1.ecosystems().stream().map(Ecosystem::toDto)).collect(toSet()).stream().toList())
                .categories(filters.stream().flatMap(e2 -> e2.categories().stream().map(ProjectCategoryReadEntity::toDto)).collect(toSet()).stream().toList())
                .totalPageNumber(totalNumberOfPage)
                .totalItemNumber(count.intValue())
                .hasMore(hasMore(pageIndex, totalNumberOfPage))
                .nextPageIndex(nextPageIndex(pageIndex, totalNumberOfPage));
    }

    @Override
    public ResponseEntity<ProjectResponse> getProject(final UUID projectId, final Boolean includeAllAvailableRepos) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser().orElse(null);
        final var userId = caller == null ? null : caller.getId();
        if (!permissionService.hasUserAccessToProject(projectId, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(projectId, userId));
        }

        final var projectEntity = projectReadRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));
        final var projectResponse = getProjectDetails(projectEntity, caller, includeAllAvailableRepos);

        return ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectResponse> getProjectBySlug(final String slug, final Boolean includeAllAvailableRepos) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser().orElse(null);
        final var userId = caller == null ? null : caller.getId();
        if (!permissionService.hasUserAccessToProject(slug, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(slug, userId));
        }

        final var projectEntity = projectReadRepository.findBySlug(slug)
                .orElseThrow(() -> notFound(format("Project %s not found", slug)));
        final var projectResponse = getProjectDetails(projectEntity, caller, includeAllAvailableRepos);

        return ok(projectResponse);
    }

    @Override
    public ResponseEntity<GoodFirstIssuesPageResponse> getProjectGoodFirstIssues(UUID projectId, Integer pageIndex, Integer pageSize) {
        final var page = githubIssueReadRepository.findGoodFirstIssuesOf(projectId, PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending()));
        return ok(new GoodFirstIssuesPageResponse()
                .issues(page.stream().map(GithubIssueReadEntity::toDto).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages())));
    }

    private ProjectResponse getProjectDetails(ProjectReadEntity project, User caller, final Boolean includeAllAvailableRepos) {
        final var topContributors = customContributorRepository.findProjectTopContributors(project.getId(), TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(project.getId(), null);
        final var leaders = projectLeadViewRepository.findProjectLeadersAndInvitedLeaders(project.getId());
        final var ecosystems = customProjectRepository.getProjectEcosystems(project.getId());
        final var hasRemainingBudget = customProjectRepository.hasRemainingBudget(project.getId());
        final var reposIndexedTimes = project.getRepos().stream()
                .map(GithubRepoViewEntity::getStats)
                .filter(Objects::nonNull)
                .map(GithubRepoStatsViewEntity::getLastIndexedAt).toList();

        record Me(Boolean isLeader, Boolean isInvitedAsProjectLead, Boolean isContributor, Boolean hasApplied) {
            public Boolean isMember() {
                return isLeader || isInvitedAsProjectLead || isContributor;
            }
        }
        final var me = isNull(caller) ? null : new Me(
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && l.getHasAcceptedInvitation()),
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && !l.getHasAcceptedInvitation()),
                contributionViewEntityRepository.countBy(caller.getGithubUserId(), project.getId()) > 0,
                applicationRepository.findByProjectIdAndApplicantId(project.getId(), caller.getGithubUserId()).isPresent()
        );

        return new ProjectResponse()
                .id(project.getId())
                .slug(project.getSlug())
                .name(project.getName())
                .createdAt(DateMapper.ofNullable(project.getCreatedAt()))
                .shortDescription(project.getShortDescription())
                .longDescription(project.getLongDescription())
                .logoUrl(project.getLogoUrl())
                .moreInfos(isNull(project.getMoreInfos()) ? null : project.getMoreInfos().stream()
                        .sorted(Comparator.comparing(ProjectMoreInfoViewEntity::getRank))
                        .map(moreInfo -> new SimpleLink().url(moreInfo.getUrl()).value(moreInfo.getName()))
                        .collect(Collectors.toList()))
                .hiring(project.getHiring())
                .visibility(mapProjectVisibility(project.getVisibility()))
                .contributorCount(contributorCount)
                .hasRemainingBudget(hasRemainingBudget)
                .rewardSettings(mapRewardSettings(new ProjectRewardSettings(
                        project.getIgnorePullRequests(),
                        project.getIgnoreIssues(),
                        project.getIgnoreCodeReviews(),
                        project.getIgnoreContributionsBefore()
                )))
                .topContributors(topContributors.stream().map(u -> new GithubUserResponse()
                        .githubUserId(u.getGithubUserId())
                        .login(u.getLogin())
                        .avatarUrl(u.getAvatarUrl())
                ).toList())
                .leaders(leaders.stream()
                        .filter(leader -> Boolean.TRUE.equals(leader.getHasAcceptedInvitation()))
                        .map(UserMapper::map)
                        .sorted(comparing(RegisteredUserResponse::getGithubUserId))
                        .collect(Collectors.toList()))
                .invitedLeaders(leaders.stream()
                        .filter(leader -> Boolean.FALSE.equals(leader.getHasAcceptedInvitation()))
                        .map(UserMapper::map)
                        .sorted(comparing(RegisteredUserResponse::getGithubUserId))
                        .collect(Collectors.toList()))
                .tags(project.getTags().stream()
                        .map(t -> switch (t.getTag()) {
                            case HOT_COMMUNITY -> ProjectTag.HOT_COMMUNITY;
                            case NEWBIES_WELCOME -> ProjectTag.NEWBIES_WELCOME;
                            case LIKELY_TO_REWARD -> ProjectTag.LIKELY_TO_REWARD;
                            case WORK_IN_PROGRESS -> ProjectTag.WORK_IN_PROGRESS;
                            case FAST_AND_FURIOUS -> ProjectTag.FAST_AND_FURIOUS;
                            case BIG_WHALE -> ProjectTag.BIG_WHALE;
                            case UPDATED_ROADMAP -> ProjectTag.UPDATED_ROADMAP;
                        })
                        .sorted(comparing(ProjectTag::name))
                        .toList())
                .ecosystems(ecosystems.stream()
                        .map(e -> new EcosystemResponse()
                                .id(e.getId())
                                .name(e.getName())
                                .slug(e.getSlug())
                                .logoUrl(e.getLogoUrl())
                                .url(e.getUrl())
                        )
                        .sorted(comparing(EcosystemResponse::getName))
                        .toList())
                .categories(project.getCategories().stream()
                        .map(c -> new ProjectCategoryResponse()
                                .id(c.id())
                                .name(c.name())
                                .iconSlug(c.iconSlug())
                        )
                        .sorted(comparing(ProjectCategoryResponse::getName))
                        .toList())
                .sponsors(project.getSponsors().stream()
                        .filter(s -> SponsorMapper.isActive(s))
                        .map(s -> s.sponsor())
                        .map(s -> new SponsorResponse()
                                .id(s.getId())
                                .name(s.getName())
                                .logoUrl(s.getLogoUrl())
                                .url(s.getUrl())
                        )
                        .sorted(comparing(SponsorResponse::getName))
                        .toList())
                .organizations(project.organizations(Boolean.TRUE.equals(includeAllAvailableRepos)))
                .languages(project.getLanguages().stream().map(LanguageReadEntity::toDto).sorted(comparing(LanguageResponse::getName)).toList())
                .indexingComplete(reposIndexedTimes.stream().noneMatch(Objects::isNull))
                .indexedAt(reposIndexedTimes.stream().filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null))
                .me(me == null ? null : new ProjectMeResponse()
                        .isMember(me.isMember())
                        .isProjectLead(me.isLeader())
                        .isInvitedAsProjectLead(me.isInvitedAsProjectLead())
                        .isContributor(me.isContributor())
                        .hasApplied(me.hasApplied()))
                .goodFirstIssueCount(project.getGoodFirstIssueCount());
    }

    @Override
    public ResponseEntity<RewardsPageResponse> getProjectRewards(UUID projectId, Integer pageIndex, Integer pageSize, List<UUID> currencies,
                                                                 List<Long> contributors, String fromDate, String toDate, RewardsSort sort,
                                                                 SortDirection direction) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);

        final User authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        if (!permissionService.isUserProjectLead(projectId, authenticatedUser.getId())) {
            throw forbidden("Only project leads can read rewards on their projects");
        }

        final var pageRequest = PageRequest.of(sanitizePageIndex, sanitizePageSize,
                RewardDetailsReadRepository.sortBy(sort, direction));

        final var page = rewardDetailsReadRepository.findProjectRewards(projectId, currencies, contributors, fromDate, toDate,
                pageRequest);
        final var budgetStats = budgetStatsReadRepository.findByProject(projectId, currencies, contributors, fromDate, toDate);

        final RewardsPageResponse rewardsPageResponse = RewardsMapper.mapProjectRewardPageToResponse(sanitizePageIndex, page, budgetStats,
                authenticatedUser.asAuthenticatedUser());

        return rewardsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardsPageResponse) :
                ResponseEntity.ok(rewardsPageResponse);
    }

    @Override
    public ResponseEntity<ContributorsPageResponse> getProjectContributors(UUID projectId,
                                                                           Integer pageIndex,
                                                                           Integer pageSize,
                                                                           String login,
                                                                           ContributorsPageSortBy sort,
                                                                           SortDirection direction,
                                                                           Boolean showHidden) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var callerIsLead = authenticatedUser.isPresent() && permissionService.isUserProjectLead(projectId, authenticatedUser.get().getId());
        final var sortBy = switch (sort) {
            case LOGIN -> "(login)";
            case CONTRIBUTION_COUNT -> "(contribution_count)";
            case REWARD_COUNT -> "(reward_count)";
            case EARNED -> "(earned)";
            case TO_REWARD_COUNT -> "(to_reward_count)";
        };
        final var sortDirection = switch (direction) {
            case ASC -> Sort.Direction.ASC;
            case DESC -> Sort.Direction.DESC;
        };
        final var pageable = PageRequest.of(sanitizePageIndex, sanitizePageSize,
                JpaSort.unsafe(sortDirection, sortBy).andUnsafe(Sort.Direction.ASC, "(login)"));

        final var contributors = projectContributorQueryRepository.findProjectContributors(projectId,
                login,
                authenticatedUser.map(User::getId).orElse(null),
                showHidden,
                pageable);
        final var hasHiddenContributors = callerIsLead && projectContributorQueryRepository.hasHiddenContributors(projectId);

        final var response = new ContributorsPageResponse()
                .contributors(contributors.stream().map(c -> c.toDto(callerIsLead)).toList())
                .hasHiddenContributors(hasHiddenContributors)
                .totalPageNumber(contributors.getTotalPages())
                .totalItemNumber((int) contributors.getTotalElements())
                .hasMore(hasMore(pageIndex, contributors.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, contributors.getTotalPages()));

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }
}
