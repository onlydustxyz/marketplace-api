package com.onlydust.customer.io.adapter.dto;

import java.util.UUID;

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

    static String getMarketplaceBillingProfileUrlFromEnvironment(String environment, UUID billingProfileId) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) +
               "settings/billing/%s/general-information".formatted(billingProfileId);
    }

    static String getMarketplaceCommitteeApplicationUrlFromEnvironment(String environment, UUID committeeId, UUID projectId) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) + "c/%s/applicant?p=%s".formatted(committeeId, projectId);
    }

    static String getMarketplaceMyRewardsUrlFromEnvironment(String environment) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) + "rewards";
    }

    static String getMarketplaceMyApplicationsFromEnvironment(String environment) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) + "applications";
    }
}
