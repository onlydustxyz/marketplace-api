package onlydust.com.marketplace.project.domain.port.input;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Language;

public interface LanguageFacadePort {
    Language createLanguage(final @NonNull String name,
                            final @NonNull String slug, 
                            final @NonNull Set<String> fileExtensions, 
                            final URI logoUrl, 
                            final URI transparentLogoUrl, 
                            final URI bannerUrl, 
                            final String color);

    List<Language> listLanguages();

    Language updateLanguage(final @NonNull Language language);

    URL uploadPicture(final @NonNull InputStream imageInputStream);

    Map<String, Optional<Language>> getKnownExtensions();
}
