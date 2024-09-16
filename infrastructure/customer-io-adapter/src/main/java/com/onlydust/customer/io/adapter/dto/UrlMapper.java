package com.onlydust.customer.io.adapter.dto;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Committee;

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

    static String getMarketplaceBillingProfileUrlFromEnvironment(String environment, BillingProfile.Id billingProfileId) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) +
               "settings/billing/%s/general-information".formatted(billingProfileId);
    }

    static String getMarketplaceCommitteeApplicationUrlFromEnvironment(String environment, Committee.Id committeeId, ProjectId projectId) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) + "c/%s/applicant?p=%s".formatted(committeeId, projectId);
    }

    static String getMarketplaceMyRewardsUrlFromEnvironment(String environment) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) + "rewards";
    }

    static String getMarketplaceMyApplicationsFromEnvironment(String environment) {
        return getMarketplaceFrontendUrlFromEnvironment(environment) + "applications";
    }

    static String getMarketplaceAdminFrontendUrlFromEnvironment(String environment) {
        return switch (environment) {
            case "develop" -> "https://develop-admin.onlydust.com/";
            case "staging" -> "https://staging-admin.onlydust.com/";
            case "production" -> "https://admin.onlydust.com/";
            default -> throw internalServerError("Invalid environment " + environment);
        };
    }
}
