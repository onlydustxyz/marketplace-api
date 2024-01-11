package onlydust.com.marketplace.api.infrastructure.coinmarketcap;


import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;

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

    @Data
    @AllArgsConstructor
    public static class Properties {
        String baseUri;
        String apiKey;
    }
}
