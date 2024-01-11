package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyMetadataService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class CmcCurrencyMetadataServiceAdapter implements CurrencyMetadataService {
    private final CmcClient client;

    @Override
    public Optional<Currency.Metadata> get(ERC20 token) {
        return client.send("/v2/cryptocurrency/info?aux=logo,description&address=" + token.address(),
                        HttpMethod.GET, null, Response.class)
                .map(this::decode);
    }

    private Currency.Metadata decode(Response response) {
        if (response.status.errorCode > 0)
            throw OnlyDustException.internalServerError("Unable to fetch token metadata [%d] %s".formatted(
                    response.status.errorCode, response.status.errorMessage));

        return response.data.values().stream().findFirst().orElseThrow().toMetadata();
    }

    private record Response(Status status, Map<Integer, Data> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Status(@JsonProperty("error_code") Integer errorCode, @JsonProperty("error_message") String errorMessage) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Data(String description, URI logo) {
            public Currency.Metadata toMetadata() {
                return new Currency.Metadata(description, logo);
            }
        }
    }
}
