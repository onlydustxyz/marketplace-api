package com.onlydust.customer.io.adapter.client;

import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class CustomerIOHttpClient extends HttpClient {

    private CustomerIOProperties customerIOProperties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder().header("Authorization", "Bearer %s".formatted(customerIOProperties.getApiKey()));
    }

    @Override
    protected URI uri(String path) {
        return URI.create(customerIOProperties.getBaseUri() + path);
    }
}
