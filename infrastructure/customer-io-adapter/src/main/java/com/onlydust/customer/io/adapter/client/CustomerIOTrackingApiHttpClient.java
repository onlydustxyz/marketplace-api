package com.onlydust.customer.io.adapter.client;

import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Base64;

@AllArgsConstructor
public class CustomerIOTrackingApiHttpClient extends HttpClient {

    private CustomerIOProperties customerIOProperties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder().header("Authorization",
                "Basic " + Base64.getEncoder().encodeToString("%s:%s".formatted(customerIOProperties.getTrackingSiteId(),
                        customerIOProperties.getTrackingApiKey()).getBytes()
        ));
    }

    @Override
    protected URI uri(String path) {
        return URI.create(customerIOProperties.getTrackingBaseUri() + path);
    }
}
