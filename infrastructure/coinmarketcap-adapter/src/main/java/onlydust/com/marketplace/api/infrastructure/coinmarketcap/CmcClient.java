package onlydust.com.marketplace.api.infrastructure.coinmarketcap;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class CmcClient extends HttpClient {
    private final Properties properties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder()
                .header("X-CMC_PRO_API_KEY", properties.apiKey);
    }

    @Override
    protected URI uri(String path) {
        return URI.create(properties.baseUri + path);
    }

    public Optional<MetadataResponse> metadata(ContractAddress address) {
        return get("/v2/cryptocurrency/info?aux=logo,description&address=%s".formatted(address), new TypeReference<Response<Map<Integer,
                MetadataResponse>>>() {
        }).flatMap(d -> d.values().stream().findFirst());
    }

    public <T> Optional<T> get(String path, TypeReference<Response<T>> typeRef) {
        return send(path, HttpMethod.GET, null, typeRef)
                .map(r -> {
                    if (r.status.errorCode > 0)
                        throw OnlyDustException.internalServerError("Unable to fetch quotes [%d] %s".formatted(r.status.errorCode, r.status.errorMessage));
                    return r.data;
                });
    }

    @Data
    @AllArgsConstructor
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
    public record MetadataResponse(String description, URI logo) {
    }
}
