package onlydust.com.marketplace.project.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "fromIso3")
@Getter
@Accessors(fluent = true)
public class OldCountry {
    private static final Map<String, String> COUNTRY_NAME_MAPPED_TO_ISO3_CODE = Arrays.stream(Locale.getISOCountries()).map(isoCountry -> new Locale("",
            isoCountry)).collect(Collectors.toMap(Locale::getISO3Country, locale -> locale.getDisplayCountry(Locale.ENGLISH)));

    private static final Set<String> EU_COUNTRIES = Set.of("AUT", "BEL", "BGR", "HRV", "CYP", "CZE", "DNK", "EST", "FIN", "FRA", "DEU", "GRC", "HUN", "IRL",
            "ITA", "LVA", "LTU", "LUX", "MLT", "NLD", "POL", "PRT", "ROU", "SVK", "SVN", "ESP", "SWE", "GBR");

    final @NonNull String iso3Code;

    public Optional<String> display() {
        return Optional.ofNullable(COUNTRY_NAME_MAPPED_TO_ISO3_CODE.get(iso3Code));
    }

    public boolean inEuropeanUnion() {
        return EU_COUNTRIES.contains(iso3Code);
    }

    public boolean isFrance() {
        return "FRA".equals(iso3Code);
    }
}
