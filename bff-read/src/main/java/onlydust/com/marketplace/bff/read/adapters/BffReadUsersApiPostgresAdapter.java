package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.BffReadUsersApi;
import onlydust.com.marketplace.api.contract.model.LanguageResponse;
import onlydust.com.marketplace.api.contract.model.UserProfileLanguagePage;
import onlydust.com.marketplace.api.contract.model.UserProfileLanguagePageItem;
import onlydust.com.marketplace.bff.read.repositories.BffLanguageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
public class BffReadUsersApiPostgresAdapter implements BffReadUsersApi {
    final BffLanguageRepository languageRepository;

    @Override
    public ResponseEntity<UserProfileLanguagePage> getUserProfileStatsPerLanguages(Long githubId, Integer pageIndex, Integer pageSize) {
        final var languages = languageRepository.findAll();
        return ok(new UserProfileLanguagePage()
                .languages(languages.stream().map(l -> new UserProfileLanguagePageItem()
                        .language(new LanguageResponse().id(l.id()).name(l.name()))).toList()));
    }
}
