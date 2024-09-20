package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LanguageEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LanguageFileExtensionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.LanguageExtensionRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.LanguageRepository;
import onlydust.com.marketplace.project.domain.model.Language;
import onlydust.com.marketplace.project.domain.port.output.LanguageStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PostgresLanguageAdapter implements LanguageStorage {
    private final LanguageRepository languageRepository;
    private final LanguageExtensionRepository languageExtensionRepository;

    @Override
    @Transactional
    public void save(Language language) {
        languageRepository.saveAndFlush(LanguageEntity.of(language));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> findAll() {
        return languageRepository.findAll().stream().map(LanguageEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Language.Id id) {
        return languageRepository.existsById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Optional<Language>> getKnownExtensions() {
        final var knownExtensions = languageExtensionRepository.findAll().stream()
                .collect(Collectors.toMap(LanguageFileExtensionEntity::extension, e -> Optional.of(e.language().toDomain())));
        languageExtensionRepository.findAllIndexed().forEach(e -> knownExtensions.putIfAbsent(e, Optional.empty()));
        return knownExtensions;
    }
}
