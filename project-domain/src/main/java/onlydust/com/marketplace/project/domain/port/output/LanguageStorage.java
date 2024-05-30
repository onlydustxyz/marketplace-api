package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Language;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LanguageStorage {
    void save(Language language);

    List<Language> findAll();

    boolean exists(Language.Id id);

    Map<String, Optional<Language>> getKnownExtensions();

    void updateProjectsLanguages();
}
