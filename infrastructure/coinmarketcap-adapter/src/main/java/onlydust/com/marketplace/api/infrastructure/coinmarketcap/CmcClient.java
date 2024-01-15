package onlydust.com.marketplace.api.infrastructure.coinmarketcap;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpMethod;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

public class CmcClient extends HttpClient {
    private final Properties properties;
    private final static Map<Currency.Id, Integer> INTERNAL_IDS = new HashMap<>();

    public CmcClient(Properties properties) {
        this.properties = properties;
        INTERNAL_IDS.putAll(properties.currencyIds);
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
        final var typeRef = new TypeReference<Response<Map<Integer, MetadataResponse>>>() {
        };
        return get("/v2/cryptocurrency/info?aux=logo,description&address=%s".formatted(erc20.address()), typeRef).flatMap(d -> d.values().stream().findFirst());
    }

    public Map<Integer, QuoteResponse> quotes(List<Currency> from, List<Currency> to) {
        final var fromIds = from.stream().map(this::internalId).filter(Optional::isPresent).map(id -> id.get().toString()).collect(Collectors.joining(","));
        final var toIds = to.stream().map(this::internalId).filter(Optional::isPresent).map(id -> id.get().toString()).collect(Collectors.joining(","));
        final var typeRef = new TypeReference<Response<Map<Integer, QuoteResponse>>>() {
        };

        return get("/v2/cryptocurrency/quotes/latest?id=%s&convert_id=%s".formatted(fromIds, toIds), typeRef)
                .orElseThrow(() -> badRequest("Unable to fetch quotes"));
    }

    public Optional<Integer> internalId(Currency currency) {
        final var id = INTERNAL_IDS.computeIfAbsent(currency.id(), i -> currency.erc20().flatMap(this::metadata).map(MetadataResponse::id).orElse(null));
        return Optional.ofNullable(id);
    }

    private <T> Optional<T> get(String path, TypeReference<Response<T>> typeRef) {
        return send(path, HttpMethod.GET, null, typeRef)
                .map(r -> {
                    if (r.status.errorCode > 0)
                        throw OnlyDustException.internalServerError("Unable to fetch quotes [%d] %s".formatted(r.status.errorCode, r.status.errorMessage));
                    return r.data;
                });
    }

    public record Properties(String baseUri, String apiKey, Map<Currency.Id, Integer> currencyIds) {
    }

    public record Response<T>(Status status, T data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Status(@JsonProperty("error_code") Integer errorCode, @JsonProperty("error_message") String errorMessage) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadataResponse(Integer id, String description, URI logo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuoteResponse(Integer id, Platform platform, Map<Integer, Quote> quote) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Quote(BigDecimal price) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Platform(@JsonProperty("token_address") ContractAddress tokenAddress) {
        }
    }
}
