package com.onlydust.customer.io.adapter.dto;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public interface UrlMapper {

    static String getMarketplaceFrontendUrlFromEnvironment(String environment) {
        return switch (environment) {
            case "develop" -> "https://develop-app.onlydust.com/";
            case "staging" -> "https://staging-app.onlydust.com/";
            case "production" -> "https://app.onlydust.com/";
            default -> throw internalServerError("Invalid environment " + environment);
        };
    }
}
