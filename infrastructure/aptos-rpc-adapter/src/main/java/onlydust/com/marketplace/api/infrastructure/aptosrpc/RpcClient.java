package onlydust.com.marketplace.api.infrastructure.aptosrpc;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Properties {
        String baseUri;
    }

    public record AccountResponse(@JsonProperty("sequence_number") String sequenceNumber, @JsonProperty("authentication_key") String authenticationKey) {
    }
}
