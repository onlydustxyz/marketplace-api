package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.BffReadUsersApi;
import onlydust.com.marketplace.api.contract.model.UserProfileLanguagePage;
import onlydust.com.marketplace.bff.read.entities.UserProfileLanguagePageItemEntity;
import onlydust.com.marketplace.bff.read.repositories.UserProfileLanguagePageItemEntityRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
public class BffReadUsersApiPostgresAdapter implements BffReadUsersApi {
    final UserProfileLanguagePageItemEntityRepository userProfileLanguagePageItemEntityRepository;

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
}
