package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.contract.ReadProjectsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectMoreInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoStatsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributionViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomContributorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectLeadViewRepository;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.accounting.AllTransactionReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategorySuggestionReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectContributorLabelReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectCustomStatReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.mapper.RewardsMapper;
import onlydust.com.marketplace.api.read.mapper.UserMapper;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.OrSlug;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.contract.model.FinancialTransactionType.*;
import static onlydust.com.marketplace.api.contract.model.GithubIssueStatus.OPEN;
import static onlydust.com.marketplace.api.read.properties.Cache.*;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseNullable;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.mapRewardSettings;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadProjectsApiPostgresAdapter implements ReadProjectsApi {
    private static final int TOP_CONTRIBUTOR_COUNT = 3;

    private final Cache cache;
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final ProjectGithubIssueItemReadRepository projectGithubIssueItemReadRepository;
    private final ProjectReadRepository projectReadRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final ContributionViewEntityRepository contributionViewEntityRepository;
    private final ProjectsPageRepository projectsPageRepository;
    private final ProjectsPageItemFiltersRepository projectsPageItemFiltersRepository;
    private final RewardDetailsReadRepository rewardDetailsReadRepository;
    private final BudgetStatsReadRepository budgetStatsReadRepository;
    private final ProjectContributorQueryRepository projectContributorQueryRepository;
    private final ProjectCustomStatsReadRepository projectCustomStatsReadRepository;
    private final AllTransactionReadRepository allTransactionReadRepository;


    private static @NonNull List<FinancialTransactionType> sanitizeTypes(List<FinancialTransactionType> types) {
        final var acceptedTypes = List.of(GRANTED, UNGRANTED, REWARDED);
        final var filteredTypes = Optional.ofNullable(types).orElse(List.of())
                .stream().filter(acceptedTypes::contains).toList();
        return filteredTypes.isEmpty() ? acceptedTypes : filteredTypes;
    }

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
        final var userId = user.map(AuthenticatedUser::id).map(UserId::value).orElse(null);

        final var projects = projectsPageRepository.findAll(userId,
                mine,
                search,
                null,
                null,
                null,
                isNull(categorySlugs) ? null : categorySlugs.toArray(String[]::new),
                null,
                isNull(languageSlugs) ? null : languageSlugs.toArray(String[]::new),
                null,
                isNull(ecosystemSlugs) ? null : ecosystemSlugs.toArray(String[]::new),
                isNull(tags) ? null : tags.stream().map(ProjectTag::name).toArray(String[]::new),
                hasGoodFirstIssues,
                PageRequest.of(pageIndex, pageSize, isNull(sort) ? JpaSort.unsafe(ASC, "project_name") :
                        switch (sort) {
                            case RANK -> JpaSort.unsafe(DESC, "rank").and(JpaSort.unsafe(ASC, "project_name"));
                            case NAME -> JpaSort.unsafe(ASC, "project_name");
                            case REPO_COUNT -> JpaSort.unsafe(DESC, "coalesce(array_length(p.repo_ids, 1), 0)").and(JpaSort.unsafe(ASC, "project_name"));
                            case CONTRIBUTOR_COUNT -> JpaSort.unsafe(DESC, "coalesce(cd.contributor_count, 0)").and(JpaSort.unsafe(ASC, "project_name"));
                        })
        );

        final var filters = projectsPageItemFiltersRepository.findFilters(userId);

        return ok()
                .cacheControl(cache.whenAnonymous(user, M))
                .body(new ProjectPageResponse()
                        .projects(projects.stream().map(p -> p.toDto(userId)).toList())
                        .categories(filters.categories().stream().sorted(comparing(ProjectCategoryResponse::getName)).toList())
                        .languages(filters.languages().stream().sorted(comparing(LanguageResponse::getName)).toList())
                        .ecosystems(filters.ecosystems().stream().sorted(comparing(EcosystemLinkResponse::getName)).toList())
                        .totalPageNumber(projects.getTotalPages())
                        .totalItemNumber((int) projects.getTotalElements())
                        .hasMore(hasMore(pageIndex, projects.getTotalPages()))
                        .nextPageIndex(nextPageIndex(pageIndex, projects.getTotalPages())));
    }

    @Override
    public ResponseEntity<ProjectResponse> getProject(final UUID projectId, final Boolean includeAllAvailableRepos) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var userId = caller.map(AuthenticatedUser::id);

        if (!permissionService.hasUserAccessToProject(ProjectId.of(projectId), userId))
            throw forbidden("Project %s is private and user %s cannot access it".formatted(projectId, userId));

        final var project = projectReadRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));

        return ok()
                .cacheControl(cache.whenAnonymous(caller, M))
                .body(getProjectDetails(project, caller.orElse(null), includeAllAvailableRepos));
    }

    @Override
    public ResponseEntity<ProjectResponse> getProjectBySlug(final String slug, final Boolean includeAllAvailableRepos) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var userId = caller.map(AuthenticatedUser::id);

        if (!permissionService.hasUserAccessToProject(slug, userId))
            throw forbidden("Project %s is private and user %s cannot access it".formatted(slug, userId));

        final var project = projectReadRepository.findBySlug(slug)
                .orElseThrow(() -> notFound(format("Project %s not found", slug)));

        return ok()
                .cacheControl(cache.whenAnonymous(caller, M))
                .body(getProjectDetails(project, caller.orElse(null), includeAllAvailableRepos));
    }

    @Override
    public ResponseEntity<GithubIssuePageResponse> getProjectGoodFirstIssues(UUID projectId, Integer pageIndex, Integer pageSize) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var page = projectGithubIssueItemReadRepository.findIssuesOf(projectId, new String[]{OPEN.name()}, false, null, true, true, false,
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
                                                                          Boolean isAvailable,
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
                isAvailable,
                isGoodFirstIssue,
                isIncludedInAnyHackathon,
                hackathonId,
                isNull(languageIds) ? null : languageIds.stream().distinct().toArray(UUID[]::new),
                search,
                PageRequest.of(pageIndex, pageSize, Sort.by(direction == SortDirection.ASC ? ASC : Sort.Direction.DESC, switch (sort) {
                    case CREATED_AT -> "i.created_at";
                    case CLOSED_AT -> "i.closed_at";
                })));
        return ok()
                .cacheControl(cache.whenAnonymous(caller, XS))
                .body(new GithubIssuePageResponse()
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
                        .sorted(comparing(ProjectMoreInfoViewEntity::getRank))
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
                        .map(e -> new EcosystemLinkResponse()
                                .id(e.getId())
                                .name(e.getName())
                                .slug(e.getSlug())
                                .logoUrl(e.getLogoUrl())
                                .url(e.getUrl())
                                .hidden(e.getHidden())
                        )
                        .sorted(comparing(EcosystemLinkResponse::getName))
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
                .contributorLabels(project.contributorLabels().stream()
                        .map(ProjectContributorLabelReadEntity::toDto)
                        .sorted(comparing(ProjectContributorLabelResponse::getName))
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
                                                                 List<Long> contributors, String fromDate, String toDate, String search,
                                                                 RewardsSort sort, SortDirection direction) {
        // TODO implement search
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);

        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        if (!permissionService.isUserProjectLead(ProjectId.of(projectId), authenticatedUser.id())) {
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
                status(PARTIAL_CONTENT).body(rewardsPageResponse) :
                ok(rewardsPageResponse);
    }

    @Override
    public ResponseEntity<ProjectStatsResponse> getProjectStats(UUID projectId, String fromDate, String toDate) {
        final var customStats = projectCustomStatsReadRepository.findById(projectId,
                parseNullable(fromDate),
                parseNullable(toDate));

        return ok(new ProjectStatsResponse()
                .activeContributorCount(customStats.map(ProjectCustomStatReadEntity::activeContributorCount).orElse(0))
                .mergedPrCount(customStats.map(ProjectCustomStatReadEntity::mergedPrCount).orElse(0))
                .onboardedContributorCount(customStats.map(ProjectCustomStatReadEntity::onboardedContributorCount).orElse(0))
        );
    }

    @Override
    public ResponseEntity<ProjectFinancialResponse> getProjectFinancialDetails(UUID projectId) {
        final var project = projectReadRepository.findStatsById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));

        return ok(project.toFinancialResponse());
    }

    @Override
    public ResponseEntity<ProjectFinancialResponse> getProjectFinancialDetailsBySlug(String projectSlug) {
        final var project = projectReadRepository.findStatsBySlug(projectSlug)
                .orElseThrow(() -> notFound(format("Project %s not found", projectSlug)));

        return ok(project.toFinancialResponse());
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
        final var callerIsLead = authenticatedUser.isPresent() && permissionService.isUserProjectLead(ProjectId.of(projectId), authenticatedUser.get().id());
        final var sortBy = switch (sort) {
            case LOGIN -> "(login)";
            case CONTRIBUTION_COUNT -> "(contribution_count)";
            case REWARD_COUNT -> "(reward_count)";
            case EARNED -> "(earned)";
            case TO_REWARD_COUNT -> "(to_reward_count)";
        };
        final var sortDirection = switch (direction) {
            case ASC -> ASC;
            case DESC -> Sort.Direction.DESC;
        };
        final var pageable = PageRequest.of(sanitizePageIndex, sanitizePageSize,
                JpaSort.unsafe(sortDirection, sortBy).andUnsafe(ASC, "(login)"));

        final var contributors = projectContributorQueryRepository.findProjectContributors(projectId,
                login,
                authenticatedUser.map(AuthenticatedUser::id).map(UserId::value).orElse(null),
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
                status(PARTIAL_CONTENT)
                        .cacheControl(cache.whenAnonymous(authenticatedUser, S))
                        .body(response) :
                ok()
                        .cacheControl(cache.whenAnonymous(authenticatedUser, S))
                        .body(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ProjectTransactionPageResponse> getProjectTransactions(String projectIdOrSlug,
                                                                                 Integer pageIndex,
                                                                                 Integer pageSize,
                                                                                 String fromDate,
                                                                                 String toDate,
                                                                                 List<FinancialTransactionType> types,
                                                                                 String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = findAccountingTransactions(projectIdOrSlug, fromDate, toDate, types, search, index, size);

        final var response = new ProjectTransactionPageResponse()
                .transactions(page.getContent().stream().map(AllTransactionReadEntity::toProjectTransactionPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }

    @GetMapping(
            value = "/api/v1/projects/{projectIdOrSlug}/transactions",
            produces = "text/csv"
    )
    @Transactional(readOnly = true)
    public ResponseEntity<String> exportProjectTransactions(@PathVariable String projectIdOrSlug,
                                                            @RequestParam(required = false) Integer pageIndex,
                                                            @RequestParam(required = false) Integer pageSize,
                                                            @RequestParam(required = false) String fromDate,
                                                            @RequestParam(required = false) String toDate,
                                                            @RequestParam(required = false) List<FinancialTransactionType> types,
                                                            @RequestParam(required = false) String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = findAccountingTransactions(projectIdOrSlug, fromDate, toDate, types, search, index, size);
        final var format = CSVFormat.DEFAULT.builder().build();
        final var sw = new StringWriter();

        try (final var printer = new CSVPrinter(sw, format)) {
            printer.printRecord("id", "timestamp", "transaction_type", "contributor_id", "program_id", "amount", "currency", "usd_amount");
            for (final var transaction : page.getContent())
                transaction.toProjectCsv(printer);
        } catch (final IOException e) {
            throw internalServerError("Error while exporting transactions to CSV", e);
        }

        final var csv = sw.toString();

        return status(hasMore(index, page.getTotalPages()) ? PARTIAL_CONTENT : OK)
                .body(csv);
    }

    private Page<AllTransactionReadEntity> findAccountingTransactions(String projectIdOrSlugStr, String fromDate, String toDate,
                                                                      List<FinancialTransactionType> types, String search, int index, int size) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var projectIdOrSlug = OrSlug.of(projectIdOrSlugStr, ProjectId::of);
        types = sanitizeTypes(types);

        if (!permissionService.isUserProjectLead(projectIdOrSlug, authenticatedUser.id()))
            throw unauthorized("User %s is not authorized to access project %s".formatted(authenticatedUser.id(), projectIdOrSlug));

        return allTransactionReadRepository.findAllForProject(
                projectIdOrSlug.uuid().orElse(null),
                projectIdOrSlug.slug().orElse(null),
                parseNullable(fromDate),
                parseNullable(toDate),
                search,
                types.stream().map(FinancialTransactionType::name).toList(),
                PageRequest.of(index, size, Sort.by("timestamp").descending())
        );
    }
}
