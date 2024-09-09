package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadBiApi;
import onlydust.com.marketplace.api.contract.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBiApiPostgresAdapter implements ReadBiApi {
    @Override
    public ResponseEntity<BiContributorsPageResponse> getBIContributors(String fromDate, String toDate, TimeGroupingEnum timeGrouping,
                                                                        ContributorTypeEnum contributorType, List<UUID> categoryIds, List<UUID> languageIds,
                                                                        List<UUID> ecosystemIds, List<String> countryCodes,
                                                                        DecimalNumberKpiFilter totalRewardedUsdAmount, NumberKpiFilter contributionCount,
                                                                        NumberKpiFilter mergedPrCount, NumberKpiFilter rewardCount) {
        return ReadBiApi.super.getBIContributors(fromDate, toDate, timeGrouping, contributorType, categoryIds, languageIds, ecosystemIds, countryCodes,
                totalRewardedUsdAmount, contributionCount, mergedPrCount, rewardCount);
    }

    @Override
    public ResponseEntity<BiContributorsStatsListResponse> getBIContributorsStats(String fromDate, String toDate, TimeGroupingEnum timeGrouping,
                                                                                  List<UUID> programOrEcosystemIds) {
        return ReadBiApi.super.getBIContributorsStats(fromDate, toDate, timeGrouping, programOrEcosystemIds);
    }

    @Override
    public ResponseEntity<BiProjectsPageResponse> getBIProjects(String fromDate, String toDate, TimeGroupingEnum timeGrouping, List<UUID> projectLeadIds,
                                                                List<UUID> categoryIds, List<UUID> languageIds, List<UUID> ecosystemIds,
                                                                DecimalNumberKpiFilter availableBudgetUsdAmount, NumberKpiFilter percentUsedBudget,
                                                                DecimalNumberKpiFilter totalGrantedUsdAmount, DecimalNumberKpiFilter averageRewardUsdAmount,
                                                                DecimalNumberKpiFilter totalRewardedUsdAmount, NumberKpiFilter onboardedContributorCount,
                                                                NumberKpiFilter activeContributorCount, NumberKpiFilter mergedPrCount,
                                                                NumberKpiFilter rewardCount, NumberKpiFilter contributionCount) {
        return ReadBiApi.super.getBIProjects(fromDate, toDate, timeGrouping, projectLeadIds, categoryIds, languageIds, ecosystemIds, availableBudgetUsdAmount
                , percentUsedBudget, totalGrantedUsdAmount, averageRewardUsdAmount, totalRewardedUsdAmount, onboardedContributorCount, activeContributorCount
                , mergedPrCount, rewardCount, contributionCount);
    }

    @Override
    public ResponseEntity<BiProjectsStatsListResponse> getBIProjectsStats(String fromDate, String toDate, TimeGroupingEnum timeGrouping,
                                                                          List<UUID> programOrEcosystemIds) {
        return ReadBiApi.super.getBIProjectsStats(fromDate, toDate, timeGrouping, programOrEcosystemIds);
    }
}
