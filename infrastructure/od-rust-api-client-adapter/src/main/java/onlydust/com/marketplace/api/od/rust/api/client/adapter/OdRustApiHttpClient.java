package onlydust.com.marketplace.api.od.rust.api.client.adapter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class OdRustApiHttpClient extends onlydust.com.marketplace.kernel.infrastructure.HttpClient {
    private final Properties properties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder()
                .header("Api-Key", properties.getApiKey());
    }

    @Override
    protected URI uri(String path) {
        return URI.create(properties.getBaseUri() + path);
    }

    @Data
    public static class Properties {
        String baseUri;
        String apiKey;
    }
}
