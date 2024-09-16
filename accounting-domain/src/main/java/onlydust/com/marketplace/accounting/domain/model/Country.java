package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.*;

import static java.util.stream.Collectors.toMap;

@AllArgsConstructor(staticName = "fromIso3")
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public class Country {
    private static final Map<String, String> COUNTRY_NAME_MAPPED_TO_ISO3_CODE = Arrays.stream(Locale.getISOCountries())
            .map(isoCountry -> new Locale("", isoCountry))
            .collect(toMap(Locale::getISO3Country, locale -> locale.getDisplayCountry(Locale.ENGLISH)));

    private static final Map<String, String> ISO3_TO_ISO2_CODES = Arrays.stream(Locale.getISOCountries())
            .map(isoCountry -> new Locale("", isoCountry))
            .collect(toMap(Locale::getISO3Country, Locale::getCountry));

    private static final Set<String> EU_COUNTRIES = Set.of("AUT", "BEL", "BGR", "HRV", "CYP", "CZE", "DNK", "EST", "FIN", "FRA", "DEU", "GRC", "HUN", "IRL",
            "ITA", "LVA", "LTU", "LUX", "MLT", "NLD", "POL", "PRT", "ROU", "SVK", "SVN", "ESP", "SWE", "GBR");

    private final @NonNull String iso3Code;

    public static Optional<Country> tryFromIso3(String iso3Code) {
        return COUNTRY_NAME_MAPPED_TO_ISO3_CODE.containsKey(iso3Code) ? Optional.of(Country.fromIso3(iso3Code)) : Optional.empty();
    }

    public @NonNull String iso2Code() {
        return ISO3_TO_ISO2_CODES.get(iso3Code);
    }

    public Optional<String> display() {
        return Optional.ofNullable(COUNTRY_NAME_MAPPED_TO_ISO3_CODE.get(iso3Code));
    }

    public boolean inEuropeanUnion() {
        return EU_COUNTRIES.contains(iso3Code);
    }

    public boolean isFrance() {
        return "FRA".equals(iso3Code);
    }

    public boolean isUsa() {
        return "USA".equals(iso3Code);
    }
}
