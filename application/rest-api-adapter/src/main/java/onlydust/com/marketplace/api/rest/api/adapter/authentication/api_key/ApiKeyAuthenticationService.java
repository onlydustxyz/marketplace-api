package onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@AllArgsConstructor
public class ApiKeyAuthenticationService {
    private static final String API_KEY_HEADER_NAME = "Api-Key";

    private final Config config;

    public Optional<Authentication> getAuthentication(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(API_KEY_HEADER_NAME))
                .filter(apiKey -> apiKey.equals(config.apiKey))
                .map(ApiKeyAuthentication::new);
    }

    @Data
    public static class Config {
        String apiKey;
        String invoiceDownloadToken;
        String baseUrl;
    }
}
