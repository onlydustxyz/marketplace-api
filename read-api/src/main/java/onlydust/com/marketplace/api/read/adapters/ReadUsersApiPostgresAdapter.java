package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadUsersApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.user.*;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

import static onlydust.com.marketplace.api.read.properties.Cache.M;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadUsersApiPostgresAdapter implements ReadUsersApi {
    final Cache cache;
    final UserProfileLanguagePageItemEntityRepository userProfileLanguagePageItemEntityRepository;
    final UserProfileEcosystemPageItemEntityRepository userProfileEcosystemPageItemEntityRepository;
    final PublicUserProfileResponseV2EntityRepository publicUserProfileResponseV2EntityRepository;
    final UserProfileProjectEarningsEntityRepository userProfileProjectEarningsEntityRepository;
    final UserWorkDistributionEntityRepository userWorkDistributionEntityRepository;
    final UserWeeklyStatsEntityRepository userWeeklyStatsEntityRepository;

    @Override
    public ResponseEntity<PublicUserProfileResponseV2> getUserProfileByLogin(String login) {
        final var userProfile = publicUserProfileResponseV2EntityRepository.findByGithubUserLogin(login)
                .orElseThrow(() -> notFound("User %s not found".formatted(login)));
        return ok()
                .cacheControl(cache.forEverybody(M))
                .body(userProfile.toDto());
    }

    @Override
    public ResponseEntity<PublicUserProfileResponseV2> getUserProfile(Long githubId) {
        final var userProfile = publicUserProfileResponseV2EntityRepository.findByGithubUserId(githubId)
                .orElseThrow(() -> notFound("User %d not found".formatted(githubId)));
        return ok()
                .cacheControl(cache.forEverybody(M))
                .body(userProfile.toDto());
    }

    @Override
    public ResponseEntity<UserProfileStatsV2> getUserProfileStats(Long githubId, UUID ecosystem, String fromDate, String toDate) {

        final var workDistribution = ecosystem != null ?
                userWorkDistributionEntityRepository.findByContributorIdAndEcosystem(githubId, ecosystem) :
                userWorkDistributionEntityRepository.findByContributorId(githubId);

        final var userWeeklyStats = ecosystem != null ?
                userWeeklyStatsEntityRepository.findByContributorIdAndEcosystem(githubId, ecosystem) :
                userWeeklyStatsEntityRepository.findByContributorId(githubId);

        final var perProjectsStats = userProfileProjectEarningsEntityRepository.findByContributorIdAndEcosystem(githubId,
                ecosystem,
                parseZonedNullable(fromDate),
                parseZonedNullable(toDate));

        return ok()
                .cacheControl(cache.forEverybody(M))
                .body(new UserProfileStatsV2()
                        .earnings(new UserProfileStatsV2Earnings()
                                .totalEarnedUsd(perProjectsStats.stream().map(UserProfileProjectEarningsEntity::totalEarnedUsd).reduce(BigDecimal.ZERO,
                                        BigDecimal::add))
                                .perProject(perProjectsStats.stream().map(UserProfileProjectEarningsEntity::toDto).toList()))
                        .workDistribution(workDistribution.map(UserWorkDistributionEntity::toDto)
                                .orElse(new UserWorkDistribution().codeReviewCount(0).issueCount(0).pullRequestCount(0)))
                        .activity(userWeeklyStats.stream().map(UserWeeklyStatsEntity::toDto).toList())
                );
    }

    @Override
    public ResponseEntity<UserProfileEcosystemPage> getUserProfileStatsPerEcosystems(Long githubId, Integer pageIndex, Integer pageSize) {
        final var page = userProfileEcosystemPageItemEntityRepository.findByContributorId(githubId, PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Direction.DESC, "contribution_count", "total_earned_usd")));
        return ok()
                .cacheControl(cache.forEverybody(M))
                .body(new UserProfileEcosystemPage()
                        .totalItemNumber((int) page.getTotalElements())
                        .totalPageNumber(page.getTotalPages())
                        .nextPageIndex(page.hasNext() ? pageIndex + 1 : pageIndex)
                        .hasMore(page.hasNext())
                        .ecosystems(page.getContent().stream().map(UserProfileEcosystemPageItemEntity::toDto).toList())
                );
    }

    @Override
    public ResponseEntity<UserProfileLanguagePage> getUserProfileStatsPerLanguages(Long githubId, Integer pageIndex, Integer pageSize) {
        final var page = userProfileLanguagePageItemEntityRepository.findByContributorId(githubId, PageRequest.of(pageIndex, pageSize,
                Sort.by(desc("contribution_count"), desc("total_earned_usd"), asc("language_name"))));
        return ok()
                .cacheControl(cache.forEverybody(M))
                .body(new UserProfileLanguagePage()
                        .totalItemNumber((int) page.getTotalElements())
                        .totalPageNumber(page.getTotalPages())
                        .nextPageIndex(page.hasNext() ? pageIndex + 1 : pageIndex)
                        .hasMore(page.hasNext())
                        .languages(page.getContent().stream().map(UserProfileLanguagePageItemEntity::toDto).toList())
                );
    }
}
