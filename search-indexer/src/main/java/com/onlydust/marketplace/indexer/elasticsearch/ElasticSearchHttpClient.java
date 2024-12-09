package com.onlydust.marketplace.indexer.elasticsearch;

import com.onlydust.marketplace.indexer.elasticsearch.properties.ElasticSearchProperties;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class ElasticSearchHttpClient extends HttpClient {

    public final ElasticSearchProperties elasticSearchProperties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder()
                .header("Authorization", "ApiKey " + elasticSearchProperties.getApiKey());
    }

    @Override
    protected URI uri(String path) {
        return URI.create(elasticSearchProperties.getUrl() + path);
    }
}
