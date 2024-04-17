package onlydust.com.marketplace.api.rest.api.adapter.authentication.token;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@AllArgsConstructor
public class QueryParamTokenAuthenticationService {
    private static final String TOKEN_QUERY_PARAM = "token";

    private final Config config;

    public Optional<Authentication> getAuthentication(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(TOKEN_QUERY_PARAM))
                .filter(apiKey -> apiKey.equals(config.token))
                .map(QueryParamTokenAuthentication::new);
    }

    @Data
    public static class Config {
        String token;
        String baseUrl;
    }
}
