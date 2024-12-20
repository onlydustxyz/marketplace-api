package onlydust.com.marketplace.api.infrastructure.coinmarketcap;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmContractAddress;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Slf4j
public class CmcClient extends HttpClient {
    private final Properties properties;
    private final static Map<Currency.Id, Integer> INTERNAL_IDS = new HashMap<>();
    private final static Map<Currency.Code, MapResponse> FIAT_CURRENCIES = new HashMap<>();

    public CmcClient(Properties properties) {
        this.properties = properties;
    }

    private Map<Currency.Code, MapResponse> fiatCurrencies() {
        if (FIAT_CURRENCIES.isEmpty()) {
            final var typeRef = new TypeReference<Response<List<MapResponse>>>() {
            };

            final var currencies = get("/v1/fiat/map?limit=5000", typeRef)
                    .orElseThrow(() -> internalServerError("Unable to fetch fiat currency map"));

            FIAT_CURRENCIES.putAll(currencies.stream().collect(Collectors.toMap(MapResponse::symbol, c -> c)));
        }

        return FIAT_CURRENCIES;
    }

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder()
                .header("X-CMC_PRO_API_KEY", properties.apiKey);
    }

    @Override
    protected URI uri(String path) {
        return URI.create(properties.baseUri + path);
    }

    public Optional<MetadataResponse> metadata(ERC20 erc20) {
        try {
            final var typeRef = new TypeReference<Response<Map<Integer, MetadataResponse>>>() {
            };
            return get("/v2/cryptocurrency/info?aux=logo,description&address=%s".formatted(erc20.getAddress()), typeRef).flatMap(d -> d.values().stream().findFirst());
        } catch (OnlyDustException e) {
            if (e.getStatus() == 400) {
                LOGGER.warn("Unable to fetch metadata for ERC20 token %s, trying with symbol %s".formatted(erc20.getAddress(), erc20.getSymbol()));
                return allMetadata(Currency.Code.of(erc20.getSymbol())).stream().filter(m -> m.category.equals("token")).findFirst();
            }
            throw e;
        }
    }

    public Optional<MetadataResponse> metadata(Currency.Code code) {
        return allMetadata(code).stream().filter(m -> m.category.equals("coin")).findFirst();
    }

    public Map<Integer, QuoteResponse> quotes(Set<Currency> from, Set<Currency> to) {
        final var fromIds = currencyToIdList(from);
        final var toIds = currencyToIdList(to);
        final var typeRef = new TypeReference<Response<Map<Integer, QuoteResponse>>>() {
        };

        return get("/v2/cryptocurrency/quotes/latest?id=%s&convert_id=%s".formatted(fromIds, toIds), typeRef)
                .orElseThrow(() -> internalServerError("Unable to fetch quotes"));
    }

    private String currencyToIdList(Set<Currency> from) {
        return from.stream()
                .map(Currency::cmcId)
                .sorted()
                .map(Object::toString)
                .collect(joining(","));
    }

    private List<MetadataResponse> allMetadata(Currency.Code code) {
        final var typeRef = new TypeReference<Response<Map<String, List<MetadataResponse>>>>() {
        };
        return get("/v2/cryptocurrency/info?aux=logo,description&symbol=%s".formatted(code), typeRef)
                .flatMap(d -> d.values().stream().findFirst())
                .orElse(List.of());
    }

    private <T> Optional<T> get(String path, TypeReference<Response<T>> typeRef) {
        return send(path, HttpMethod.GET, null, typeRef)
                .map(r -> {
                    if (r.status.errorCode > 0)
                        throw OnlyDustException.internalServerError("Unable to fetch quotes [%d] %s".formatted(r.status.errorCode, r.status.errorMessage));
                    return r.data;
                });
    }

    public int fiatId(Currency.@NonNull Code code) {
        return Optional.ofNullable(fiatCurrencies().get(code))
                .map(MapResponse::id)
                .orElseThrow(() -> internalServerError("Could not find fiat currency %s".formatted(code)));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Properties {
        String baseUri;
        String apiKey;
    }

    public record Response<T>(Status status, T data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Status(@JsonProperty("error_code") Integer errorCode, @JsonProperty("error_message") String errorMessage) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadataResponse(Integer id, String description, URI logo, String category, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuoteResponse(Integer id, Platform platform, Map<Integer, Quote> quote) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Quote(BigDecimal price, @JsonProperty("last_updated") Date lastUpdated) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Platform(@JsonProperty("token_address") EvmContractAddress tokenAddress) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MapResponse(Integer id, String name, String sign, Currency.Code symbol) {
    }
}
