package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.model.Language;
import onlydust.com.marketplace.project.domain.port.input.LanguageFacadePort;
import onlydust.com.marketplace.project.domain.port.output.LanguageStorage;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;


@AllArgsConstructor
public class LanguageService implements LanguageFacadePort {
    private final LanguageStorage languageStorage;
    private final ImageStoragePort imageStoragePort;

    @Override
    public Language createLanguage(final @NonNull String name, final @NonNull String slug, final @NonNull Set<String> fileExtensions) {
        final var language = Language.of(name, slug, fileExtensions);
        languageStorage.save(language);
        return language;
    }

    @Override
    public List<Language> listLanguages() {
        return languageStorage.findAll();
    }

    @Override
    public Language updateLanguage(Language language) {
        if (!languageStorage.exists(language.id())) {
            throw notFound("Language %s not found".formatted(language.id()));
        }
        languageStorage.save(language);
        return language;
    }

    @Override
    public URL uploadPicture(InputStream imageInputStream) {
        return imageStoragePort.storeImage(imageInputStream);
    }

    @Override
    public Map<String, Optional<Language>> getKnownExtensions() {
        return languageStorage.getKnownExtensions();
    }

}
