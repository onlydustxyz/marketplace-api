package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadLanguagesApi;
import onlydust.com.marketplace.api.contract.model.LanguagesResponse;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.repositories.LanguageReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadLanguagesApiPostgresAdapter implements ReadLanguagesApi {

    private final LanguageReadRepository languageReadRepository;

    @Override
    public ResponseEntity<LanguagesResponse> getAllLanguages() {
        final LanguagesResponse languagesResponse = new LanguagesResponse();
        languageReadRepository.findAll().stream()
                .map(LanguageReadEntity::toDto)
                .forEach(languagesResponse::addLanguagesItem);
        return ResponseEntity.ok(languagesResponse);
    }
}
