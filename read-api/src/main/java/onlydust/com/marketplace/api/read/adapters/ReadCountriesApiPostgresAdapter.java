package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadCountriesApi;
import onlydust.com.marketplace.api.contract.model.CountriesResponse;
import onlydust.com.marketplace.api.contract.model.CountryResponse;
import onlydust.com.marketplace.api.read.entities.CountryReadEntity;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.CountryReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static onlydust.com.marketplace.api.read.properties.Cache.M;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadCountriesApiPostgresAdapter implements ReadCountriesApi {

    private final Cache cache;
    private final CountryReadRepository countryReadRepository;

    @Override
    public ResponseEntity<CountriesResponse> getAllCountries(String search) {
        final var countries = countryReadRepository.findAllContributorCountries()
                .stream()
                .map(CountryReadEntity::country)
                .filter(c -> c.display().orElse(c.iso3Code()).toLowerCase().contains(Optional.ofNullable(search).orElse("").toLowerCase()))
                .map(c -> new CountryResponse().code(c.iso2Code()))
                .toList();

        return ok()
                .cacheControl(cache.forEverybody(M))
                .body(new CountriesResponse()
                        .countries(countries));
    }
}
