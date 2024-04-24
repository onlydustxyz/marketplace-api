package onlydust.com.marketplace.api.infrastructure.aptosrpc;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
public class RpcClient extends HttpClient {
    private final Properties properties;

    public RpcClient(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder();
    }

    @Override
    protected URI uri(String path) {
        return URI.create(properties.baseUri + path);
    }

    public Optional<AccountResponse> getAccount(String address) {
        return send("/accounts/%s".formatted(address), HttpMethod.GET, null, AccountResponse.class);
    }

    public Optional<TransactionResponse> getTransactionByHash(String hash) {
        return send("/transactions/by_hash/%s".formatted(hash), HttpMethod.GET, null, TransactionResponse.class);
    }

    public Optional<TransactionResourceResponse> getAccountResource(String hash, String resource) {
        return send("/accounts/%s/resource/%s".formatted(hash, URLEncoder.encode(resource, StandardCharsets.UTF_8)), HttpMethod.GET, null,
                TransactionResourceResponse.class);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Properties {
        String baseUri;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountResponse(@JsonProperty("sequence_number") String sequenceNumber, @JsonProperty("authentication_key") String authenticationKey) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionResponse(Long version, String hash, Long timestamp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionResourceResponse(String type, Data data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Data(java.lang.Integer decimals, String name, String symbol, Supply supply) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Supply(List<Data> vec) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Data(Integer integer) {
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Integer(List<Data> vec) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Data(BigInteger limit, BigInteger value) {
            }
        }
    }
}
