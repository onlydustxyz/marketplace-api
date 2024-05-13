package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadUsersApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.bff.read.entities.UserProfileEcosystemPageItemEntity;
import onlydust.com.marketplace.bff.read.entities.UserProfileLanguagePageItemEntity;
import onlydust.com.marketplace.bff.read.entities.UserProfileProjectEarningsEntity;
import onlydust.com.marketplace.bff.read.repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadUsersApiPostgresAdapter implements ReadUsersApi {
    final UserProfileLanguagePageItemEntityRepository userProfileLanguagePageItemEntityRepository;
    final UserProfileEcosystemPageItemEntityRepository userProfileEcosystemPageItemEntityRepository;
    final PublicUserProfileResponseV2EntityRepository publicUserProfileResponseV2EntityRepository;
    final UserProfileProjectEarningsEntityRepository userProfileProjectEarningsEntityRepository;
    final UserWorkDistributionEntityRepository userWorkDistributionEntityRepository;

    @Override
    public ResponseEntity<PublicUserProfileResponseV2> getUserProfileByLogin(String login) {
        final var userProfile = publicUserProfileResponseV2EntityRepository.findByGithubUserLogin(login)
                .orElseThrow(() -> notFound("User %s not found".formatted(login)));
        return ok(userProfile.toDto());
    }

    @Override
    public ResponseEntity<UserProfileStatsV2> getUserProfileStats(Long githubId, UUID ecosystem) {
        final var workDistribution = userWorkDistributionEntityRepository.findByContributorId(githubId)
                .orElseThrow(() -> notFound("User %d not found".formatted(githubId)));
        final var perProjectsStats = userProfileProjectEarningsEntityRepository.findByContributorId(githubId);

        return ok(new UserProfileStatsV2()
                .earnings(new UserProfileStatsV2Earnings()
                        .totalEarnedUsd(perProjectsStats.stream().map(UserProfileProjectEarningsEntity::totalEarnedUsd).reduce(BigDecimal.ZERO,
                                BigDecimal::add))
                        .perProject(perProjectsStats.stream().map(UserProfileProjectEarningsEntity::toDto).toList()))
                .workDistribution(workDistribution.toDto())
        );
    }

    @Override
    public ResponseEntity<UserProfileEcosystemPage> getUserProfileStatsPerEcosystems(Long githubId, Integer pageIndex, Integer pageSize) {
        final var page = userProfileEcosystemPageItemEntityRepository.findByContributorId(githubId, PageRequest.of(pageIndex, pageSize));
        return ok(new UserProfileEcosystemPage()
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .nextPageIndex(page.hasNext() ? pageIndex + 1 : pageIndex)
                .hasMore(page.hasNext())
                .ecosystems(page.getContent().stream().map(UserProfileEcosystemPageItemEntity::toDto).toList())
        );
    }

    @Override
    public ResponseEntity<UserProfileLanguagePage> getUserProfileStatsPerLanguages(Long githubId, Integer pageIndex, Integer pageSize) {
        final var page = userProfileLanguagePageItemEntityRepository.findByContributorId(githubId, PageRequest.of(pageIndex, pageSize));
        return ok(new UserProfileLanguagePage()
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .nextPageIndex(page.hasNext() ? pageIndex + 1 : pageIndex)
                .hasMore(page.hasNext())
                .languages(page.getContent().stream().map(UserProfileLanguagePageItemEntity::toDto).toList())
        );
    }

    @Override
    public ResponseEntity<PublicUserProfileResponseV2> getUserProfile(Long githubId) {
        final var userProfile = publicUserProfileResponseV2EntityRepository.findByGithubUserId(githubId)
                .orElseThrow(() -> notFound("User %d not found".formatted(githubId)));
        return ok(userProfile.toDto());
    }
}
