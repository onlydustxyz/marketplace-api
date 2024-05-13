package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadUsersApi;
import onlydust.com.marketplace.api.contract.model.PublicUserProfileResponseV2;
import onlydust.com.marketplace.api.contract.model.UserProfileEcosystemPage;
import onlydust.com.marketplace.api.contract.model.UserProfileLanguagePage;
import onlydust.com.marketplace.bff.read.entities.UserProfileEcosystemPageItemEntity;
import onlydust.com.marketplace.bff.read.entities.UserProfileLanguagePageItemEntity;
import onlydust.com.marketplace.bff.read.repositories.PublicUserProfileResponseV2EntityRepository;
import onlydust.com.marketplace.bff.read.repositories.UserProfileEcosystemPageItemEntityRepository;
import onlydust.com.marketplace.bff.read.repositories.UserProfileLanguagePageItemEntityRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadUsersApiPostgresAdapter implements ReadUsersApi {
    final UserProfileLanguagePageItemEntityRepository userProfileLanguagePageItemEntityRepository;
    final UserProfileEcosystemPageItemEntityRepository userProfileEcosystemPageItemEntityRepository;
    final PublicUserProfileResponseV2EntityRepository publicUserProfileResponseV2EntityRepository;

    @Override
    public ResponseEntity<PublicUserProfileResponseV2> getUserProfileByLogin(String login) {
        final var userProfile = publicUserProfileResponseV2EntityRepository.findByGithubUserLogin(login)
                .orElseThrow(() -> OnlyDustException.notFound("User %s not found".formatted(login)));
        return ok(userProfile.toDto());
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
                .orElseThrow(() -> OnlyDustException.notFound("User %d not found".formatted(githubId)));
        return ok(userProfile.toDto());
    }
}
