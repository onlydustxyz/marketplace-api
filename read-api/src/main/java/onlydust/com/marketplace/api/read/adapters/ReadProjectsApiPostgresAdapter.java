package onlydust.com.marketplace.api.read.adapters;

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
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.project.*;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import onlydust.com.marketplace.api.read.mapper.RewardsMapper;
import onlydust.com.marketplace.api.read.mapper.UserMapper;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static onlydust.com.marketplace.api.contract.model.GithubIssueStatus.OPEN;
import static onlydust.com.marketplace.api.read.entities.project.ProjectPageItemQueryEntity.*;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.mapRewardSettings;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadProjectsApiPostgresAdapter implements ReadProjectsApi {
    private static final int TOP_CONTRIBUTOR_COUNT = 3;

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final ProjectGithubIssueItemReadRepository projectGithubIssueItemReadRepository;
    private final ProjectReadRepository projectReadRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final ContributionViewEntityRepository contributionViewEntityRepository;
    private final ProjectsPageRepository projectsPageRepository;
    private final ProjectsPageFiltersRepository projectsPageFiltersRepository;
    private final RewardDetailsReadRepository rewardDetailsReadRepository;
    private final BudgetStatsReadRepository budgetStatsReadRepository;
    private final ProjectContributorQueryRepository projectContributorQueryRepository;
    private final ProjectCustomStatsReadRepository projectCustomStatsReadRepository;

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
                                                           final ProjectListSort sort
    ) {
        final var user = authenticatedAppUserService.tryGetAuthenticatedUser();

        final String ecosystemsJsonPath = getEcosystemsJsonPath(ecosystemSlugs);
        final String tagsJsonPath = getTagsJsonPath(isNull(tags) ? null : tags.stream().map(Enum::name).toList());
        final String languagesJsonPath = getLanguagesJsonPath(languageSlugs);

        return ResponseEntity.ok(user.map(u -> getProjectsForAuthenticatedUser(u.id(), mine, search, ecosystemsJsonPath, tagsJsonPath, languagesJsonPath,
                        categorySlugs, hasGoodFirstIssues, sanitizePageIndex(pageIndex), sanitizePageSize(pageSize), sort))
                .orElseGet(() -> getProjectsForAnonymousUser(search, ecosystemsJsonPath, tagsJsonPath, languagesJsonPath, categorySlugs, hasGoodFirstIssues,
                        sanitizePageIndex(pageIndex), sanitizePageSize(pageSize), sort)));
    }

    private ProjectPageResponse getProjectsForAuthenticatedUser(UUID userId,
                                                                Boolean mine,
                                                                String search,
                                                                String ecosystemsJsonPath, String tagsJsonPath, String languagesJsonPath,
                                                                List<String> categorySlugs, Boolean hasGoodFirstIssues,
                                                                Integer pageIndex, Integer pageSize, ProjectListSort sortBy) {
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
                isNull(sortBy) ? ProjectListSort.NAME.name() : sortBy.name(),
                PaginationMapper.getPostgresOffsetFromPagination(pageSize, pageIndex),
                pageSize);
        final var filters = projectsPageFiltersRepository.findFiltersForUser(userId, mine);
        final var totalNumberOfPage = calculateTotalNumberOfPage(pageSize, count.intValue());

        return toProjectPage(userId, pageIndex, projects, filters, totalNumberOfPage, count);
    }

    private ProjectPageResponse getProjectsForAnonymousUser(String search,
                                                            String ecosystemsJsonPath, String tagsJsonPath, String languagesJsonPath,
                                                            List<String> categorySlugs, Boolean hasGoodFirstIssues,
                                                            Integer pageIndex, Integer pageSize, ProjectListSort sortBy) {
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
                isNull(sortBy) ? ProjectListSort.NAME.name() : sortBy.name(),
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
        final var userId = caller == null ? null : caller.id();

        if (!permissionService.hasUserAccessToProject(projectId, userId))
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(projectId, userId));

        final var project = projectReadRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));

        return ok(getProjectDetails(project, caller, includeAllAvailableRepos));
    }

    @Override
    public ResponseEntity<ProjectResponse> getProjectBySlug(final String slug, final Boolean includeAllAvailableRepos) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser().orElse(null);
        final var userId = caller == null ? null : caller.id();

        if (!permissionService.hasUserAccessToProject(slug, userId))
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(slug, userId));

        final var project = projectReadRepository.findBySlug(slug)
                .orElseThrow(() -> notFound(format("Project %s not found", slug)));

        return ok(getProjectDetails(project, caller, includeAllAvailableRepos));
    }

    @Override
    public ResponseEntity<GithubIssuePageResponse> getProjectGoodFirstIssues(UUID projectId, Integer pageIndex, Integer pageSize) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var page = projectGithubIssueItemReadRepository.findIssuesOf(projectId, new String[]{OPEN.name()}, false, null, true, false,
                null, null, null, PageRequest.of(pageIndex, pageSize, Sort.by("i.created_at").descending()));
        return ok(new GithubIssuePageResponse()
                .issues(page.stream().map(i -> i.toPageItemResponse(caller.map(AuthenticatedUser::githubUserId).orElse(null))).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages())));
    }

    @Override
    public ResponseEntity<GithubIssuePageResponse> getProjectPublicIssues(UUID projectId,
                                                                          Integer pageIndex,
                                                                          Integer pageSize,
                                                                          UUID hackathonId,
                                                                          List<UUID> languageIds,
                                                                          List<GithubIssueStatus> statuses,
                                                                          Boolean isAssigned,
                                                                          Boolean isApplied,
                                                                          Boolean isGoodFirstIssue,
                                                                          Boolean isIncludedInAnyHackathon,
                                                                          String search,
                                                                          @NotNull ProjectIssuesSort sort,
                                                                          SortDirection direction) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var page = projectGithubIssueItemReadRepository.findIssuesOf(projectId,
                isNull(statuses) ? null : statuses.stream().distinct().map(Enum::name).toArray(String[]::new),
                isAssigned,
                isApplied,
                isGoodFirstIssue,
                isIncludedInAnyHackathon,
                hackathonId,
                isNull(languageIds) ? null : languageIds.stream().distinct().toArray(UUID[]::new),
                search,
                PageRequest.of(pageIndex, pageSize, Sort.by(direction == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC, switch (sort) {
                    case CREATED_AT -> "i.created_at";
                    case CLOSED_AT -> "i.closed_at";
                })));
        return ok(new GithubIssuePageResponse()
                .issues(page.stream().map(i -> i.toPageItemResponse(caller.map(AuthenticatedUser::githubUserId).orElse(null))).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages())));
    }

    private ProjectResponse getProjectDetails(ProjectReadEntity project, AuthenticatedUser caller, final Boolean includeAllAvailableRepos) {
        final var topContributors = customContributorRepository.findProjectTopContributors(project.id(), TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(project.id(), null);
        final var leaders = projectLeadViewRepository.findProjectLeadersAndInvitedLeaders(project.id());
        final var ecosystems = customProjectRepository.getProjectEcosystems(project.id());
        final var hasRemainingBudget = customProjectRepository.hasRemainingBudget(project.id());
        final var reposIndexedTimes = project.repos().stream()
                .map(GithubRepoViewEntity::getStats)
                .filter(Objects::nonNull)
                .map(GithubRepoStatsViewEntity::getLastIndexedAt).toList();

        record Me(Boolean isLeader, Boolean isInvitedAsProjectLead, Boolean isContributor) {
            public Boolean isMember() {
                return isLeader || isInvitedAsProjectLead || isContributor;
            }
        }
        final var me = isNull(caller) ? null : new Me(
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.githubUserId()) && l.getHasAcceptedInvitation()),
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.githubUserId()) && !l.getHasAcceptedInvitation()),
                contributionViewEntityRepository.countBy(caller.githubUserId(), project.id()) > 0
        );

        return new ProjectResponse()
                .id(project.id())
                .slug(project.slug())
                .name(project.name())
                .createdAt(DateMapper.ofNullable(project.createdAt()))
                .shortDescription(project.shortDescription())
                .longDescription(project.longDescription())
                .logoUrl(project.logoUrl())
                .moreInfos(isNull(project.moreInfos()) ? null : project.moreInfos().stream()
                        .sorted(Comparator.comparing(ProjectMoreInfoViewEntity::getRank))
                        .map(moreInfo -> new SimpleLink().url(moreInfo.getUrl()).value(moreInfo.getName()))
                        .collect(Collectors.toList()))
                .hiring(project.hiring())
                .visibility(project.visibility())
                .contributorCount(contributorCount)
                .hasRemainingBudget(hasRemainingBudget)
                .rewardSettings(mapRewardSettings(new ProjectRewardSettings(
                        project.ignorePullRequests(),
                        project.ignoreIssues(),
                        project.ignoreCodeReviews(),
                        project.ignoreContributionsBefore()
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
                .tags(project.tags().stream()
                        .map(t -> switch (t.getTag()) {
                            case HOT_COMMUNITY -> ProjectTag.HOT_COMMUNITY;
                            case NEWBIES_WELCOME -> ProjectTag.NEWBIES_WELCOME;
                            case LIKELY_TO_REWARD -> ProjectTag.LIKELY_TO_REWARD;
                            case WORK_IN_PROGRESS -> ProjectTag.WORK_IN_PROGRESS;
                            case FAST_AND_FURIOUS -> ProjectTag.FAST_AND_FURIOUS;
                            case BIG_WHALE -> ProjectTag.BIG_WHALE;
                            case UPDATED_ROADMAP -> ProjectTag.UPDATED_ROADMAP;
                            case HAS_GOOD_FIRST_ISSUES -> ProjectTag.HAS_GOOD_FIRST_ISSUES;
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
                .categories(project.categories().stream()
                        .map(c -> new ProjectCategoryResponse()
                                .id(c.id())
                                .name(c.name())
                                .iconSlug(c.iconSlug())
                        )
                        .sorted(comparing(ProjectCategoryResponse::getName))
                        .toList())
                .categorySuggestions(project.categorySuggestions().stream()
                        .map(ProjectCategorySuggestionReadEntity::name)
                        .sorted()
                        .toList())
                .programs(project.programs().stream()
                        .sorted(comparing(ProgramReadEntity::name))
                        .map(ProgramReadEntity::toShortResponse)
                        .toList())
                .organizations(project.organizations(Boolean.TRUE.equals(includeAllAvailableRepos)))
                .languages(project.languages().stream().map(LanguageReadEntity::toDto).sorted(comparing(LanguageResponse::getName)).toList())
                .indexingComplete(reposIndexedTimes.stream().noneMatch(Objects::isNull))
                .indexedAt(reposIndexedTimes.stream().filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null))
                .me(me == null ? null : new ProjectMeResponse()
                        .isMember(me.isMember())
                        .isProjectLead(me.isLeader())
                        .isInvitedAsProjectLead(me.isInvitedAsProjectLead())
                        .isContributor(me.isContributor()))
                .goodFirstIssueCount(project.goodFirstIssues().size());
    }

    @Override
    public ResponseEntity<RewardsPageResponse> getProjectRewards(UUID projectId, Integer pageIndex, Integer pageSize, List<UUID> currencies,
                                                                 List<Long> contributors, String fromDate, String toDate, RewardsSort sort,
                                                                 SortDirection direction) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);

        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        if (!permissionService.isUserProjectLead(projectId, authenticatedUser.id())) {
            throw forbidden("Only project leads can read rewards on their projects");
        }

        final var pageRequest = PageRequest.of(sanitizePageIndex, sanitizePageSize,
                RewardDetailsReadRepository.sortBy(sort, direction));

        final var page = rewardDetailsReadRepository.findProjectRewards(projectId, currencies, contributors, fromDate, toDate,
                pageRequest);
        final var budgetStats = budgetStatsReadRepository.findByProject(projectId, currencies, contributors, fromDate, toDate);

        final RewardsPageResponse rewardsPageResponse = RewardsMapper.mapProjectRewardPageToResponse(sanitizePageIndex, page, budgetStats,
                authenticatedUser);

        return rewardsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(rewardsPageResponse) :
                ResponseEntity.ok(rewardsPageResponse);
    }

    @Override
    public ResponseEntity<ProjectStatsResponse> getProjectStats(UUID projectId, String fromDate, String toDate) {
        final var customStats = projectCustomStatsReadRepository.findById(projectId,
                onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseNullable(fromDate),
                onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseNullable(toDate));

        final var project = projectReadRepository.findStatsById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));

        return ok(new ProjectStatsResponse()
                .activeContributorCount(customStats.map(ProjectCustomStatReadEntity::activeContributorCount).orElse(0))
                .mergedPrCount(customStats.map(ProjectCustomStatReadEntity::mergedPrCount).orElse(0))
                .onboardedContributorCount(customStats.map(ProjectCustomStatReadEntity::onboardedContributorCount).orElse(0))
                .totalGranted(DetailedTotalMoneyMapper.map(project.globalStatsPerCurrency(), ProjectStatPerCurrencyReadEntity::totalGranted))
                .totalRewarded(DetailedTotalMoneyMapper.map(project.globalStatsPerCurrency(), ProjectStatPerCurrencyReadEntity::totalRewarded))
        );
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
        final var callerIsLead = authenticatedUser.isPresent() && permissionService.isUserProjectLead(projectId, authenticatedUser.get().id());
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
                authenticatedUser.map(AuthenticatedUser::id).orElse(null),
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
