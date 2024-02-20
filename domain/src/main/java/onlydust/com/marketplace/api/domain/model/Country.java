package onlydust.com.marketplace.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "fromIso3")
@Getter
@Accessors(fluent = true)
public class Country {
    private static final Map<String, String> COUNTRY_NAME_MAPPED_TO_ISO3_CODE = Arrays.stream(Locale.getISOCountries()).map(isoCountry -> new Locale("",
                    isoCountry))
            .collect(Collectors.toMap(Locale::getISO3Country, locale -> locale.getDisplayCountry(Locale.ENGLISH)));

    final @NonNull String iso3Code;

    public Optional<String> display() {
        return Optional.ofNullable(COUNTRY_NAME_MAPPED_TO_ISO3_CODE.get(iso3Code));
    }
}
