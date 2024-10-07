package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.ReadBiApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedKpisReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.ContributorKpisReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.ProjectKpisReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.WorldMapKpiReadEntity;
import onlydust.com.marketplace.api.read.entities.program.BiFinancialMonthlyStatsReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.api.contract.model.FinancialTransactionType.*;
import static onlydust.com.marketplace.api.read.repositories.BiFinancialMonthlyStatsReadRepository.IdGrouping.*;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBiApiPostgresAdapter implements ReadBiApi {
    private static final ZonedDateTime DEFAULT_FROM_DATE = ZonedDateTime.parse("2007-10-20T05:24:19Z");
    private final PermissionService permissionService;
    private final AggregatedKpisReadRepository aggregatedKpisReadRepository;
    private final WorldMapKpiReadRepository worldMapKpiReadRepository;
    private final ProjectKpisReadRepository projectKpisReadRepository;
    private final ContributorKpisReadRepository contributorKpisReadRepository;
    private final PermissionService permissionsService;
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final BiFinancialMonthlyStatsReadRepository biFinancialMonthlyStatsReadRepository;
    private final ProjectReadRepository projectReadRepository;

    private static ZonedDateTime sanitizedDate(String fromDate, ZonedDateTime defaultFromDate) {
        return Optional.ofNullable(DateMapper.parseNullable(fromDate)).map(DateMapper::toZoneDateTime).orElse(defaultFromDate);
    }

    @Override
    public ResponseEntity<BiContributorsPageResponse> getBIContributors(BiContributorsQueryParams q) {
        final var page = findContributors(q);

        return ok(new BiContributorsPageResponse()
                .contributors(page.stream().map(ContributorKpisReadEntity::toDto).toList())
                .hasMore(hasMore(q.getPageIndex(), page.getTotalPages()))
                .nextPageIndex(nextPageIndex(q.getPageIndex(), page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages()));
    }

    @GetMapping(
            value = "/api/v1/bi/contributors",
            produces = "text/csv"
    )
    public ResponseEntity<String> exportBIContributors(BiContributorsQueryParams q) {

        final var page = findContributors(q);
        final var format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .build();
        final var sw = new StringWriter();

        try (final var printer = new CSVPrinter(sw, format)) {
            printer.printRecord("contributor",
                    "projects",
                    "categories",
                    "languages",
                    "ecosystems",
                    "country",
                    "total_rewarded_usd_amount",
                    "reward_count",
                    "issue_count",
                    "pr_count",
                    "code_review_count",
                    "contribution_count");
            for (final var transaction : page.getContent())
                transaction.toCsv(printer);
        } catch (final IOException e) {
            throw internalServerError("Error while exporting to CSV", e);
        }

        final var csv = sw.toString();

        return status(hasMore(q.getPageIndex(), page.getTotalPages()) ? PARTIAL_CONTENT : OK)
                .body(csv);
    }

    @Override
    public ResponseEntity<BiContributorsStatsListResponse> getBIContributorsStats(TimeGroupingEnum timeGrouping, String fromDate, String toDate,
                                                                                  List<UUID> dataSourceIds) {
        final var statsPerTimestamp = aggregatedKpisReadRepository.findAllContributors(
                        timeGrouping,
                        timeGrouping == TimeGroupingEnum.QUARTER ? "3 MONTHS" : "1 %s".formatted(timeGrouping.name()),
                        parseZonedNullable(fromDate),
                        parseZonedNullable(toDate),
                        getFilteredDataSourceIds(dataSourceIds)).stream()
                .collect(Collectors.toMap(AggregatedKpisReadEntity::timestamp, Function.identity()));

        final var mergedStats = statsPerTimestamp.keySet().stream().map(timestamp -> {
                    var stats = statsPerTimestamp.get(timestamp);
                    var statsOfPreviousPeriod = statsPerTimestamp.get(stats.timestampOfPreviousPeriod());
                    return stats.toContributorDto(statsOfPreviousPeriod);
                })
                .sorted(comparing(BiContributorsStatsListItemResponse::getTimestamp))
                .skip(1) // Skip the first element as it is the previous period used to compute churned contributor count
                .toList();

        return ok(new BiContributorsStatsListResponse().stats(mergedStats));
    }

    @Override
    public ResponseEntity<BiProjectsPageResponse> getBIProjects(BiProjectsQueryParams q) {

        final var page = findProjects(q);

        return ok(new BiProjectsPageResponse()
                .projects(page.stream().map(ProjectKpisReadEntity::toDto).toList())
                .hasMore(hasMore(q.getPageIndex(), page.getTotalPages()))
                .nextPageIndex(nextPageIndex(q.getPageIndex(), page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages()));
    }

    @GetMapping(
            value = "/api/v1/bi/projects",
            produces = "text/csv"
    )
    public ResponseEntity<String> exportBIProjects(BiProjectsQueryParams q) {

        final var page = findProjects(q);
        final var format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .build();
        final var sw = new StringWriter();

        try (final var printer = new CSVPrinter(sw, format)) {
            printer.printRecord("project",
                    "leads",
                    "categories",
                    "languages",
                    "ecosystems",
                    "programs",
                    "available_budget_usd_amount",
                    "available_budgets",
                    "percent_used_budget",
                    "total_granted_usd_amount",
                    "total_rewarded_usd_amount",
                    "average_reward_usd_amount",
                    "onboarded_contributor_count",
                    "active_contributor_count",
                    "reward_count",
                    "issue_count",
                    "pr_count",
                    "code_review_count",
                    "contribution_count");
            for (final var transaction : page.getContent())
                transaction.toCsv(printer);
        } catch (final IOException e) {
            throw internalServerError("Error while exporting to CSV", e);
        }

        final var csv = sw.toString();

        return status(hasMore(q.getPageIndex(), page.getTotalPages()) ? PARTIAL_CONTENT : OK)
                .body(csv);
    }

    @Override
    public ResponseEntity<BiProjectsStatsListResponse> getBIProjectsStats(TimeGroupingEnum timeGrouping,
                                                                          String fromDate,
                                                                          String toDate,
                                                                          List<UUID> dataSourceIds) {

        final var statsPerTimestamp = aggregatedKpisReadRepository.findAllProjects(
                        timeGrouping,
                        timeGrouping == TimeGroupingEnum.QUARTER ? "3 MONTHS" : "1 %s".formatted(timeGrouping.name()),
                        parseZonedNullable(fromDate),
                        sanitizedDate(toDate, ZonedDateTime.now()),
                        getFilteredDataSourceIds(dataSourceIds)).stream()
                .collect(Collectors.toMap(AggregatedKpisReadEntity::timestamp, Function.identity()));

        final var mergedStats = statsPerTimestamp.keySet().stream().map(timestamp -> {
                    var stats = statsPerTimestamp.get(timestamp);
                    var statsOfPreviousPeriod = statsPerTimestamp.get(stats.timestampOfPreviousPeriod());
                    return stats.toProjectDto(statsOfPreviousPeriod);
                })
                .sorted(comparing(BiProjectsStatsListItemResponse::getTimestamp))
                .skip(1) // Skip the first element as it is the previous period used to compute churned project count
                .toList();

        return ok(new BiProjectsStatsListResponse().stats(mergedStats));
    }

    @Override
    public ResponseEntity<List<BiWorldMapItemResponse>> getBIWorldMap(WorldMapKpiEnum kpi,
                                                                      String fromDate,
                                                                      String toDate,
                                                                      List<UUID> dataSourceIds) {

        final var kpis = switch (kpi) {
            case ACTIVE_CONTRIBUTORS -> worldMapKpiReadRepository.findActiveContributorCount(
                    parseZonedNullable(fromDate),
                    parseZonedNullable(toDate),
                    getFilteredDataSourceIds(dataSourceIds)
            );
        };

        return ok(kpis.stream()
                .map(WorldMapKpiReadEntity::toListItemResponse)
                .toList());
    }

    private UUID[] getFilteredDataSourceIds(List<UUID> dataSourceIds) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var userProjectOrProgramOrEcosystemIds = Stream.concat(
                        permissionsService.getLedProjectIds(authenticatedUser.id()).stream().map(ProjectId::value),
                        Stream.concat(permissionsService.getLedProgramIds(authenticatedUser.id()).stream().map(ProgramId::value),
                                permissionsService.getLedEcosystemIds(authenticatedUser.id()).stream().map(EcosystemId::value)))
                .toList();

        return Optional.ofNullable(dataSourceIds)
                .map(l -> l.stream().filter(userProjectOrProgramOrEcosystemIds::contains).toArray(UUID[]::new))
                .orElse(userProjectOrProgramOrEcosystemIds.toArray(UUID[]::new));
    }

    private Page<ContributorKpisReadEntity> findContributors(BiContributorsQueryParams q) {
        final var sanitizedFromDate = sanitizedDate(q.getFromDate(), DEFAULT_FROM_DATE).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = sanitizedDate(q.getToDate(), ZonedDateTime.now()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        final var fromDateOfPreviousPeriod = sanitizedFromDate.minusSeconds(sanitizedToDate.toEpochSecond() - sanitizedFromDate.toEpochSecond());

        return contributorKpisReadRepository.findAll(
                sanitizedFromDate,
                sanitizedToDate,
                fromDateOfPreviousPeriod,
                sanitizedFromDate,
                getFilteredDataSourceIds(q.getDataSourceIds()),
                q.getShowFilteredKpis(),
                q.getSearch(),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getCategoryIds() == null ? null : q.getCategoryIds().toArray(UUID[]::new),
                q.getLanguageIds() == null ? null : q.getLanguageIds().toArray(UUID[]::new),
                q.getEcosystemIds() == null ? null : q.getEcosystemIds().toArray(UUID[]::new),
                q.getCountryCodes() == null ? null : q.getCountryCodes().stream().map(c -> Country.fromIso2(c).iso3Code()).toArray(String[]::new),
                q.getContributionStatuses() == null ? null : q.getContributionStatuses().stream().map(Enum::name).toArray(String[]::new),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getGte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getEq).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(ContributorsQueryParamsContributionCount::getLte).orElse(null),
                q.getContributionCount() == null ? null : q.getContributionCount().getTypes().stream().map(Enum::name).toArray(String[]::new),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ContributorKpisReadRepository.getSortProperty(q.getSort())))
        );
    }

    private Page<ProjectKpisReadEntity> findProjects(BiProjectsQueryParams q) {
        final var sanitizedFromDate = sanitizedDate(q.getFromDate(), DEFAULT_FROM_DATE).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = sanitizedDate(q.getToDate(), ZonedDateTime.now()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        final var fromDateOfPreviousPeriod = sanitizedFromDate.minusSeconds(sanitizedToDate.toEpochSecond() - sanitizedFromDate.toEpochSecond());

        return projectKpisReadRepository.findAll(
                sanitizedFromDate,
                sanitizedToDate,
                fromDateOfPreviousPeriod,
                sanitizedFromDate,
                getFilteredDataSourceIds(q.getDataSourceIds()),
                q.getShowFilteredKpis(),
                q.getSearch(),
                q.getProgramIds() == null ? null : q.getProgramIds().toArray(UUID[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getProjectLeadIds() == null ? null : q.getProjectLeadIds().toArray(UUID[]::new),
                q.getCategoryIds() == null ? null : q.getCategoryIds().toArray(UUID[]::new),
                q.getLanguageIds() == null ? null : q.getLanguageIds().toArray(UUID[]::new),
                q.getEcosystemIds() == null ? null : q.getEcosystemIds().toArray(UUID[]::new),
                Optional.ofNullable(q.getAvailableBudgetUsdAmount()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getAvailableBudgetUsdAmount()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getAvailableBudgetUsdAmount()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getPercentUsedBudget()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getPercentUsedBudget()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getPercentUsedBudget()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getTotalGrantedUsdAmount()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getTotalGrantedUsdAmount()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getTotalGrantedUsdAmount()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getAverageRewardUsdAmount()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getAverageRewardUsdAmount()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getAverageRewardUsdAmount()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getTotalRewardedUsdAmount()).map(DecimalNumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getOnboardedContributorCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getOnboardedContributorCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getOnboardedContributorCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getActiveContributorCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getActiveContributorCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getActiveContributorCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(BiProjectsQueryParamsContributionCount::getGte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(BiProjectsQueryParamsContributionCount::getEq).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(BiProjectsQueryParamsContributionCount::getLte).orElse(null),
                q.getContributionCount() == null ? null : q.getContributionCount().getTypes().stream().map(Enum::name).toArray(String[]::new),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ProjectKpisReadRepository.getSortProperty(q.getSort())))
        );
    }

    @Override
    public ResponseEntity<BiFinancialsStatsListResponse> getBIFinancialsStats(String fromDate,
                                                                              String toDate,
                                                                              String search,
                                                                              UUID sponsorId,
                                                                              UUID programId,
                                                                              UUID projectId,
                                                                              String projectSlug,
                                                                              List<FinancialTransactionType> types,
                                                                              Boolean showEmpty,
                                                                              FinancialTransactionStatsSort sort,
                                                                              SortDirection sortDirection) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (sponsorId == null && programId == null && projectId == null && projectSlug == null)
            throw badRequest("At least one of sponsorId, programId or projectId or projectSlug must be provided");

        final var idGrouping = sponsorId != null ? SPONSOR_ID : programId != null ? PROGRAM_ID : PROJECT_ID;

        final var id = switch (idGrouping) {
            case SPONSOR_ID -> sponsorId;
            case PROGRAM_ID -> programId;
            case PROJECT_ID -> Optional.ofNullable(projectId).orElseGet(() -> projectReadRepository.findBySlug(projectSlug)
                    .orElseThrow(() -> notFound("Project with slug %s not found".formatted(projectSlug))).id());
        };

        switch (idGrouping) {
            case SPONSOR_ID -> {
                if (!permissionService.isUserSponsorLead(authenticatedUser.id(), SponsorId.of(id)))
                    throw unauthorized("User %s is not authorized to access sponsor %s".formatted(authenticatedUser.id(), id));
            }
            case PROGRAM_ID -> {
                if (!permissionService.hasUserAccessToProgram(authenticatedUser.id(), ProgramId.of(id)))
                    throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), id));

            }
            case PROJECT_ID -> {
                if (!permissionService.isUserProjectLead(ProjectId.of(id), authenticatedUser.id()))
                    throw unauthorized("User %s is not authorized to access project %s".formatted(authenticatedUser.id(), id));
            }
        }

        final var allTypes = switch (idGrouping) {
            case SPONSOR_ID -> List.of(DEPOSITED, ALLOCATED, UNALLOCATED);
            case PROGRAM_ID -> List.of(ALLOCATED, UNALLOCATED, GRANTED, UNGRANTED);
            case PROJECT_ID -> List.of(GRANTED, UNGRANTED, REWARDED);
        };

        final var stats = biFinancialMonthlyStatsReadRepository.findAll(id,
                        idGrouping,
                        toZoneDateTime(DateMapper.parseNullable(fromDate)),
                        toZoneDateTime(DateMapper.parseNullable(toDate)),
                        search,
                        Optional.ofNullable(types).orElse(allTypes).stream().map(FinancialTransactionType::name).toList())
                .stream()
                .collect(groupingBy(BiFinancialMonthlyStatsReadEntity::date));

        final var comparison = switch (sort) {
            default -> comparing(BiFinancialsStatsResponse::getDate);
        };

        final var response = new BiFinancialsStatsListResponse()
                .stats(stats.entrySet().stream().map(e -> new BiFinancialsStatsResponse()
                                .date(e.getKey().toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                                .totalDeposited(DetailedTotalMoneyMapper.map(e.getValue(), BiFinancialMonthlyStatsReadEntity::totalDeposited))
                                .totalAllocated(DetailedTotalMoneyMapper.map(e.getValue(), BiFinancialMonthlyStatsReadEntity::totalAllocated))
                                .totalGranted(DetailedTotalMoneyMapper.map(e.getValue(), BiFinancialMonthlyStatsReadEntity::totalGranted))
                                .totalRewarded(DetailedTotalMoneyMapper.map(e.getValue(), BiFinancialMonthlyStatsReadEntity::totalRewarded))
                                .transactionCount(e.getValue().stream().mapToInt(BiFinancialMonthlyStatsReadEntity::transactionCount).sum()))
                        .filter(r -> !FALSE.equals(showEmpty) || r.getTransactionCount() > 0)
                        .sorted(sortDirection == SortDirection.ASC ? comparison : comparison.reversed())
                        .toList());

        return ok(response);
    }
}
