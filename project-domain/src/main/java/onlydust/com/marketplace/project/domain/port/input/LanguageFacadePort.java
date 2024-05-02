package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.Language;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface LanguageFacadePort {
    Language createLanguage(String name, Set<String> fileExtensions);

    List<Language> listLanguages();

    Language updateLanguage(Language language);

    URL uploadPicture(InputStream imageInputStream);

    Map<String, Optional<Language>> getKnownExtensions();
}
