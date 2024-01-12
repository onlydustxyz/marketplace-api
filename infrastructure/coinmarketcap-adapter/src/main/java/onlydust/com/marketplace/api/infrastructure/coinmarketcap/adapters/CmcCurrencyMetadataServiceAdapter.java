package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyMetadataService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient.Response;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class CmcCurrencyMetadataServiceAdapter implements CurrencyMetadataService {
    private final CmcClient client;

    @Override
    public Optional<Currency.Metadata> get(ERC20 token) {
        final var typeRef = new TypeReference<Response<Map<String, Data>>>() {
        };

        return client.get("/v2/cryptocurrency/info?aux=logo,description&address=" + token.address(), typeRef)
                .map(d -> d.values().stream().findFirst().orElseThrow().toMetadata());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Data(String description, URI logo) {
        public Currency.Metadata toMetadata() {
            return new Currency.Metadata(description, logo);
        }
    }
}
