package onlydust.com.marketplace.api.indexer.api.client.adapter;

import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class IndexerApiHttpClient extends HttpClient {
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
