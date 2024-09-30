package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.ReadBiApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.*;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.ProgramId;
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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
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
    private final AggregatedProjectKpisReadRepository aggregatedProjectKpisReadRepository;
    private final AggregatedContributorKpisReadRepository aggregatedContributorKpisReadRepository;
    private final WorldMapKpiReadRepository worldMapKpiReadRepository;
    private final ProjectKpisReadRepository projectKpisReadRepository;
    private final ContributorKpisReadRepository contributorKpisReadRepository;
    private final PermissionService permissionsService;
    private final AuthenticatedAppUserService authenticatedAppUserService;

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
                                                                                  List<UUID> programOrEcosystemIds) {
        final var statsPerTimestamp = aggregatedContributorKpisReadRepository.findAll(
                        timeGrouping,
                        timeGrouping == TimeGroupingEnum.QUARTER ? "3 MONTHS" : "1 %s".formatted(timeGrouping.name()),
                        sanitizedDate(fromDate, DEFAULT_FROM_DATE),
                        sanitizedDate(toDate, ZonedDateTime.now()),
                        getFilteredProgramOrEcosystemIds(programOrEcosystemIds)).stream()
                .collect(Collectors.toMap(AggregatedContributorKpisReadEntity::timestamp, Function.identity()));

        final var mergedStats = statsPerTimestamp.keySet().stream().map(timestamp -> {
                    var stats = statsPerTimestamp.get(timestamp);
                    var statsOfPreviousPeriod = statsPerTimestamp.get(stats.timestampOfPreviousPeriod());
                    return stats.toDto(statsOfPreviousPeriod);
                })
                .sorted(Comparator.comparing(BiContributorsStatsListItemResponse::getTimestamp))
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
                                                                          List<UUID> programOrEcosystemIds) {
        final var statsPerTimestamp = aggregatedProjectKpisReadRepository.findAll(
                        timeGrouping,
                        timeGrouping == TimeGroupingEnum.QUARTER ? "3 MONTHS" : "1 %s".formatted(timeGrouping.name()),
                        sanitizedDate(fromDate, DEFAULT_FROM_DATE),
                        sanitizedDate(toDate, ZonedDateTime.now()),
                        getFilteredProgramOrEcosystemIds(programOrEcosystemIds)).stream()
                .collect(Collectors.toMap(AggregatedProjectKpisReadEntity::timestamp, Function.identity()));

        final var mergedStats = statsPerTimestamp.keySet().stream().map(timestamp -> {
                    var stats = statsPerTimestamp.get(timestamp);
                    var statsOfPreviousPeriod = statsPerTimestamp.get(stats.timestampOfPreviousPeriod());
                    return stats.toDto(statsOfPreviousPeriod);
                })
                .sorted(Comparator.comparing(BiProjectsStatsListItemResponse::getTimestamp))
                .skip(1) // Skip the first element as it is the previous period used to compute churned project count
                .toList();

        return ok(new BiProjectsStatsListResponse().stats(mergedStats));
    }

    @Override
    public ResponseEntity<List<BiWorldMapItemResponse>> getBIWorldMap(WorldMapKpiEnum kpi,
                                                                      String fromDate,
                                                                      String toDate,
                                                                      List<UUID> programOrEcosystemIds) {
        final var filteredProgramOrEcosystemIds = getFilteredProgramOrEcosystemIds(programOrEcosystemIds);

        final var kpis = switch (kpi) {
            case ACTIVE_CONTRIBUTORS -> worldMapKpiReadRepository.findActiveContributorCount(
                    parseZonedNullable(fromDate),
                    parseZonedNullable(toDate),
                    filteredProgramOrEcosystemIds
            );
        };

        return ok(kpis.stream()
                .map(WorldMapKpiReadEntity::toListItemResponse)
                .toList());
    }

    private static ZonedDateTime sanitizedDate(String fromDate, ZonedDateTime defaultFromDate) {
        return Optional.ofNullable(DateMapper.parseNullable(fromDate)).map(DateMapper::toZoneDateTime).orElse(defaultFromDate);
    }

    private UUID[] getFilteredProgramOrEcosystemIds(List<UUID> programOrEcosystemIds) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var userProgramOrEcosystemIds = Stream.concat(
                        permissionsService.getLedProgramIds(authenticatedUser.id()).stream().map(ProgramId::value),
                        permissionsService.getLedEcosystemIds(authenticatedUser.id()).stream().map(EcosystemId::value))
                .toList();

        return Optional.ofNullable(programOrEcosystemIds)
                .map(l -> l.stream().filter(userProgramOrEcosystemIds::contains).toArray(UUID[]::new))
                .orElse(userProgramOrEcosystemIds.toArray(UUID[]::new));
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
                getFilteredProgramOrEcosystemIds(q.getProgramOrEcosystemIds()),
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
                Optional.ofNullable(q.getIssueCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getIssueCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getIssueCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getPrCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getPrCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getPrCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getCodeReviewCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getCodeReviewCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getCodeReviewCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getLte).orElse(null),
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
                getFilteredProgramOrEcosystemIds(q.getProgramOrEcosystemIds()),
                q.getShowFilteredKpis(),
                q.getSearch(),
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
                Optional.ofNullable(q.getIssueCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getIssueCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getIssueCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getPrCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getPrCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getPrCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getCodeReviewCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getCodeReviewCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getCodeReviewCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getLte).orElse(null),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ProjectKpisReadRepository.getSortProperty(q.getSort())))
        );
    }
}
