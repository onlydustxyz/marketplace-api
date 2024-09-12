package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadBiApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.BiAggregatedProjectGrantStatsReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.BiAggregatedProjectStatsReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.BiTimestampSeriesReadEntity;
import onlydust.com.marketplace.api.read.repositories.BiAggregatedProjectGrantStatsReadRepository;
import onlydust.com.marketplace.api.read.repositories.BiAggregatedProjectStatsReadRepository;
import onlydust.com.marketplace.api.read.repositories.BiTimestampSeriesReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.api.read.entities.bi.WorldMapKpiReadEntity;
import onlydust.com.marketplace.api.read.repositories.WorldMapKpiReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBiApiPostgresAdapter implements ReadBiApi {
    private final BiAggregatedProjectStatsReadRepository biAggregatedProjectStatsReadRepository;
    private final BiAggregatedProjectGrantStatsReadRepository biAggregatedProjectGrantStatsReadRepository;
    private final BiTimestampSeriesReadRepository biTimestampSeriesReadRepository;

    private static final ZonedDateTime DEFAULT_FROM_DATE = ZonedDateTime.parse("2020-01-01T00:00:00Z");
    private final WorldMapKpiReadRepository worldMapKpiReadRepository;



    @Override
    public ResponseEntity<BiContributorsPageResponse> getBIContributors(TimeGroupingEnum timeGrouping, String fromDate, String toDate,
                                                                        ContributorTypeEnum contributorType, List<UUID> categoryIds, List<UUID> languageIds,
                                                                        List<UUID> ecosystemIds, List<String> countryCodes,
                                                                        DecimalNumberKpiFilter totalRewardedUsdAmount, NumberKpiFilter contributionCount,
                                                                        NumberKpiFilter mergedPrCount, NumberKpiFilter rewardCount) {
        return ReadBiApi.super.getBIContributors(timeGrouping, fromDate, toDate, contributorType, categoryIds, languageIds, ecosystemIds, countryCodes,
                totalRewardedUsdAmount, contributionCount, mergedPrCount, rewardCount);
    }

    @Override
    public ResponseEntity<BiContributorsStatsListResponse> getBIContributorsStats(TimeGroupingEnum timeGrouping, String fromDate, String toDate,
                                                                                  List<UUID> programOrEcosystemIds) {
        return ReadBiApi.super.getBIContributorsStats(timeGrouping, fromDate, toDate, programOrEcosystemIds);
    }

    @Override
    public ResponseEntity<BiProjectsPageResponse> getBIProjects(TimeGroupingEnum timeGrouping, String fromDate, String toDate, List<UUID> projectLeadIds,
                                                                List<UUID> categoryIds, List<UUID> languageIds, List<UUID> ecosystemIds,
                                                                DecimalNumberKpiFilter availableBudgetUsdAmount, NumberKpiFilter percentUsedBudget,
                                                                DecimalNumberKpiFilter totalGrantedUsdAmount, DecimalNumberKpiFilter averageRewardUsdAmount,
                                                                DecimalNumberKpiFilter totalRewardedUsdAmount, NumberKpiFilter onboardedContributorCount,
                                                                NumberKpiFilter activeContributorCount, NumberKpiFilter mergedPrCount,
                                                                NumberKpiFilter rewardCount, NumberKpiFilter contributionCount) {
        return ReadBiApi.super.getBIProjects(timeGrouping, fromDate, toDate, projectLeadIds, categoryIds, languageIds, ecosystemIds, availableBudgetUsdAmount
                , percentUsedBudget, totalGrantedUsdAmount, averageRewardUsdAmount, totalRewardedUsdAmount, onboardedContributorCount, activeContributorCount
                , mergedPrCount, rewardCount, contributionCount);
    }

    @Override
    public ResponseEntity<BiProjectsStatsListResponse> getBIProjectsStats(TimeGroupingEnum timeGrouping, String fromDate, String toDate,
                                                                          List<UUID> programOrEcosystemIds) {
        final var fromTime = Optional.ofNullable(DateMapper.parseNullable(fromDate)).map(DateMapper::toZoneDateTime).orElse(DEFAULT_FROM_DATE);
        final var toTime = Optional.ofNullable(DateMapper.parseNullable(toDate)).map(DateMapper::toZoneDateTime).orElse(ZonedDateTime.now());
        final var timeGroupingValue = timeGrouping.name();

        final var timestampList = biTimestampSeriesReadRepository.generateSeries(timeGroupingValue, fromTime, toTime).stream()
                .map(BiTimestampSeriesReadEntity::timestamp).toList();

        final var projectStatsPerTimestamp = biAggregatedProjectStatsReadRepository.findAll(timeGroupingValue, fromTime, toTime,
                        programOrEcosystemIds).stream()
                .collect(Collectors.toMap(BiAggregatedProjectStatsReadEntity::timestamp, Function.identity()));

        final var grantStatsPerTimestamp = biAggregatedProjectGrantStatsReadRepository.findAll(timeGroupingValue, fromTime, toTime).stream()
                .collect(Collectors.toMap(BiAggregatedProjectGrantStatsReadEntity::timestamp, Function.identity()));

        final var mergedStats = timestampList.stream().map(timestamp -> {
                    var projectStats = Optional.ofNullable(projectStatsPerTimestamp.get(timestamp));
                    var projectStatsOfPreviousPeriod =
                            projectStats.flatMap(stat -> Optional.ofNullable(projectStatsPerTimestamp.get(stat.timestampOfPreviousPeriod())));
                    var grantStats = Optional.ofNullable(grantStatsPerTimestamp.get(timestamp));

                    return new BiProjectsStatsListItemResponse()
                            .timestamp(timestamp)
                            .activeProjectCount(projectStats.map(BiAggregatedProjectStatsReadEntity::activeProjectCount).orElse(0))
                            .newProjectCount(projectStats.map(BiAggregatedProjectStatsReadEntity::newProjectCount).orElse(0))
                            .reactivatedProjectCount(projectStats.map(BiAggregatedProjectStatsReadEntity::reactivatedProjectCount).orElse(0))
                            .churnedProjectCount(projectStatsOfPreviousPeriod.map(BiAggregatedProjectStatsReadEntity::nextPeriodChurnedProjectCount).orElse(0))
                            .mergedPrCount(projectStats.map(BiAggregatedProjectStatsReadEntity::mergedPrCount).orElse(0))
                            .totalGranted(grantStats.map(BiAggregatedProjectGrantStatsReadEntity::totalGrantedUsdAmount).orElse(BigDecimal.ZERO));
                })
                .sorted(Comparator.comparing(BiProjectsStatsListItemResponse::getTimestamp))
                .toList();

        return ResponseEntity.ok(new BiProjectsStatsListResponse().stats(mergedStats));
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
}
