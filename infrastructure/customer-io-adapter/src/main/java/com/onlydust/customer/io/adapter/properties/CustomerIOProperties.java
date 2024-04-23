package com.onlydust.customer.io.adapter.properties;

import lombok.NonNull;

public record CustomerIOProperties(@NonNull String baseUri,
                                   @NonNull String apiKey) {
}
