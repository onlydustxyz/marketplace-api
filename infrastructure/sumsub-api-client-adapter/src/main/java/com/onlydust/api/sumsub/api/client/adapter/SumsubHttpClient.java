package com.onlydust.api.sumsub.api.client.adapter;

import lombok.AllArgsConstructor;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class SumsubHttpClient extends onlydust.com.marketplace.kernel.infrastructure.HttpClient {

    private final SumsubClientProperties sumsubClientProperties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder();
    }

    @Override
    protected URI uri(String path) {
        return URI.create(sumsubClientProperties.getBaseUri() + path);
    }
}
