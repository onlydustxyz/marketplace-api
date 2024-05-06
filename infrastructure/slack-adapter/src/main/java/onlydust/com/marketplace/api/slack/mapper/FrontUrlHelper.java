package onlydust.com.marketplace.api.slack.mapper;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public interface FrontUrlHelper {

    static String getMarketplaceFrontendUrlFromEnvironment(String environment) {
        return switch (environment) {
            case "develop" -> "https://develop-app.onlydust.com/";
            case "staging" -> "https://staging-app.onlydust.com/";
            case "production" -> "https://app.onlydust.com/";
            default -> throw internalServerError("Invalid environment " + environment);
        };
    }

    static String getBackOfficeFrontendUrlFromEnvironment(String environment) {
        return switch (environment) {
            case "develop" -> "https://develop-bo.onlydust.com/";
            case "staging" -> "https://staging-bo.onlydust.com/";
            case "production" -> "https://app.onlydust.bo/";
            default -> throw internalServerError("Invalid environment " + environment);
        };
    }

}
