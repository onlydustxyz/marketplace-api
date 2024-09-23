package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadBiApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedContributorKpisReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.AggregatedProjectKpisReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.ProjectKpisReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.WorldMapKpiReadEntity;
import onlydust.com.marketplace.api.read.repositories.AggregatedContributorKpisReadRepository;
import onlydust.com.marketplace.api.read.repositories.AggregatedProjectKpisReadRepository;
import onlydust.com.marketplace.api.read.repositories.ProjectKpisReadRepository;
import onlydust.com.marketplace.api.read.repositories.WorldMapKpiReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.EcosystemId;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBiApiPostgresAdapter implements ReadBiApi {
    private static final ZonedDateTime DEFAULT_FROM_DATE = ZonedDateTime.parse("2022-12-01T00:00:00Z");
    private final AggregatedProjectKpisReadRepository aggregatedProjectKpisReadRepository;
    private final AggregatedContributorKpisReadRepository aggregatedContributorKpisReadRepository;
    private final WorldMapKpiReadRepository worldMapKpiReadRepository;
    private final ProjectKpisReadRepository projectKpisReadRepository;
    private final PermissionService permissionsService;
    private final AuthenticatedAppUserService authenticatedAppUserService;

    @Override
    public ResponseEntity<BiContributorsPageResponse> getBIContributors(BiContributorsQueryParams queryParams) {
        return ReadBiApi.super.getBIContributors(queryParams);
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

        final var sanitizedFromDate = sanitizedDate(q.getFromDate(), DEFAULT_FROM_DATE);
        final var sanitizedToDate = sanitizedDate(q.getToDate(), ZonedDateTime.now());
        final var fromDateOfPreviousPeriod = sanitizedFromDate.minusSeconds(sanitizedToDate.toEpochSecond() - sanitizedFromDate.toEpochSecond());

        final var page = projectKpisReadRepository.findAll(
                sanitizedFromDate,
                sanitizedToDate,
                fromDateOfPreviousPeriod,
                sanitizedFromDate,
                getFilteredProgramOrEcosystemIds(q.getProgramOrEcosystemIds()),
                q.getSearch(),
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
                Optional.ofNullable(q.getMergedPrCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getMergedPrCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getMergedPrCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getRewardCount()).map(NumberKpiFilter::getLte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getGte).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getEq).orElse(null),
                Optional.ofNullable(q.getContributionCount()).map(NumberKpiFilter::getLte).orElse(null),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ProjectKpisReadRepository.getSortProperty(q.getSort())))
        );

        return ok(new BiProjectsPageResponse()
                .projects(page.stream().map(ProjectKpisReadEntity::toDto).toList())
                .hasMore(hasMore(q.getPageIndex(), page.getTotalPages()))
                .nextPageIndex(nextPageIndex(q.getPageIndex(), page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages()));
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
}
