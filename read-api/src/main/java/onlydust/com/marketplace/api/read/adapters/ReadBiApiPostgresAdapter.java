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
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
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


    @Override
    public ResponseEntity<BiContributorsPageResponse> getBIContributors(Integer pageIndex, Integer pageSize, TimeGroupingEnum timeGrouping,
                                                                        SortDirection direction, String fromDate, String toDate,
                                                                        ContributorTypeEnum contributorType, List<UUID> categoryIds, List<UUID> languageIds,
                                                                        List<UUID> ecosystemIds, List<String> countryCodes,
                                                                        DecimalNumberKpiFilter totalRewardedUsdAmount, NumberKpiFilter contributionCount,
                                                                        NumberKpiFilter mergedPrCount, NumberKpiFilter rewardCount) {
        return ReadBiApi.super.getBIContributors(pageIndex, pageSize, timeGrouping, direction, fromDate, toDate, contributorType, categoryIds, languageIds,
                ecosystemIds, countryCodes, totalRewardedUsdAmount, contributionCount, mergedPrCount, rewardCount);
    }

    @Override
    public ResponseEntity<BiContributorsStatsListResponse> getBIContributorsStats(TimeGroupingEnum timeGrouping, String fromDate, String toDate,
                                                                                  List<UUID> programOrEcosystemIds) {
        final var statsPerTimestamp = aggregatedContributorKpisReadRepository.findAll(
                        timeGrouping,
                        timeGrouping == TimeGroupingEnum.QUARTER ? "3 MONTHS" : "1 %s".formatted(timeGrouping.name()),
                        sanitizedDate(fromDate, DEFAULT_FROM_DATE),
                        sanitizedDate(toDate, ZonedDateTime.now()),
                        programOrEcosystemIds == null ? null : programOrEcosystemIds.toArray(UUID[]::new)).stream()
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
    public ResponseEntity<BiProjectsPageResponse> getBIProjects(Integer pageIndex, Integer pageSize, SortDirection direction,
                                                                String fromDate, String toDate, List<UUID> projectLeadIds,
                                                                List<UUID> categoryIds, List<UUID> languageIds, List<UUID> ecosystemIds,
                                                                DecimalNumberKpiFilter availableBudgetUsdAmount, NumberKpiFilter percentUsedBudget,
                                                                DecimalNumberKpiFilter totalGrantedUsdAmount, DecimalNumberKpiFilter averageRewardUsdAmount,
                                                                DecimalNumberKpiFilter totalRewardedUsdAmount, NumberKpiFilter onboardedContributorCount,
                                                                NumberKpiFilter activeContributorCount, NumberKpiFilter mergedPrCount,
                                                                NumberKpiFilter rewardCount, NumberKpiFilter contributionCount,
                                                                ProjectKpiSortEnum sort) {

        final var sanitizedFromDate = sanitizedDate(fromDate, DEFAULT_FROM_DATE);
        final var sanitizedToDate = sanitizedDate(toDate, ZonedDateTime.now());
        final var fromDateOfPreviousPeriod = sanitizedFromDate.minusSeconds(sanitizedToDate.toEpochSecond() - sanitizedFromDate.toEpochSecond());

        final var page = projectKpisReadRepository.findAll(
                sanitizedFromDate,
                sanitizedToDate,
                fromDateOfPreviousPeriod,
                sanitizedFromDate,
                projectLeadIds == null ? null : projectLeadIds.toArray(UUID[]::new),
                categoryIds == null ? null : categoryIds.toArray(UUID[]::new),
                languageIds == null ? null : languageIds.toArray(UUID[]::new),
                ecosystemIds == null ? null : ecosystemIds.toArray(UUID[]::new),
                availableBudgetUsdAmount.getGte(),
                availableBudgetUsdAmount.getEq(),
                availableBudgetUsdAmount.getLte(),
                percentUsedBudget.getGte(),
                percentUsedBudget.getEq(),
                percentUsedBudget.getLte(),
                totalGrantedUsdAmount.getGte(),
                totalGrantedUsdAmount.getEq(),
                totalGrantedUsdAmount.getLte(),
                averageRewardUsdAmount.getGte(),
                averageRewardUsdAmount.getEq(),
                averageRewardUsdAmount.getLte(),
                totalRewardedUsdAmount.getGte(),
                totalRewardedUsdAmount.getEq(),
                totalRewardedUsdAmount.getLte(),
                onboardedContributorCount.getGte(),
                onboardedContributorCount.getEq(),
                onboardedContributorCount.getLte(),
                activeContributorCount.getGte(),
                activeContributorCount.getEq(),
                activeContributorCount.getLte(),
                mergedPrCount.getGte(),
                mergedPrCount.getEq(),
                mergedPrCount.getLte(),
                rewardCount.getGte(),
                rewardCount.getEq(),
                rewardCount.getLte(),
                contributionCount.getGte(),
                contributionCount.getEq(),
                contributionCount.getLte(),
                PageRequest.of(pageIndex, pageSize, Sort.by(direction == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        ProjectKpisReadRepository.getSortProperty(sort)))
        );
        return ok(new BiProjectsPageResponse()
                .projects(page.stream().map(ProjectKpisReadEntity::toDto).toList())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages()));
    }

    @Override
    public ResponseEntity<BiProjectsStatsListResponse> getBIProjectsStats(TimeGroupingEnum timeGrouping, String fromDate, String toDate,
                                                                          List<UUID> programOrEcosystemIds) {
        final var statsPerTimestamp = aggregatedProjectKpisReadRepository.findAll(
                        timeGrouping,
                        timeGrouping == TimeGroupingEnum.QUARTER ? "3 MONTHS" : "1 %s".formatted(timeGrouping.name()),
                        sanitizedDate(fromDate, DEFAULT_FROM_DATE),
                        sanitizedDate(toDate, ZonedDateTime.now()),
                        programOrEcosystemIds == null ? null : programOrEcosystemIds.toArray(UUID[]::new)).stream()
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
        final var kpis = switch (kpi) {
            case ACTIVE_CONTRIBUTORS -> worldMapKpiReadRepository.findActiveContributorCount(
                    parseZonedNullable(fromDate),
                    parseZonedNullable(toDate),
                    programOrEcosystemIds == null ? null : programOrEcosystemIds.toArray(UUID[]::new)
            );
        };

        return ok(kpis.stream()
                .map(WorldMapKpiReadEntity::toListItemResponse)
                .toList());
    }

    private static ZonedDateTime sanitizedDate(String fromDate, ZonedDateTime defaultFromDate) {
        return Optional.ofNullable(DateMapper.parseNullable(fromDate)).map(DateMapper::toZoneDateTime).orElse(defaultFromDate);
    }
}
