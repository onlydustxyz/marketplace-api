package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Base64;
import java.util.Collection;

@AllArgsConstructor
@Slf4j
public class HasuraJwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final HasuraProperties hasuraProperties;

    public Authentication getAuthenticationFromJwt(final String authorizationBearer) {
        try {
            final String[] chunks = authorizationBearer.split("\\.");
            final String payload = chunks[1];
            final String header = chunks[0];
            String tokenWithoutSignature = header + "." + payload;
            String signature = chunks[2];

            final JwtSecret jwtSecret = objectMapper.readValue(hasuraProperties.getSecret(), JwtSecret.class);

            if (!Base64.getUrlDecoder().decode(header).equals("HS256")) {

            }
            final String expectedSignature =
                    new HmacUtils("HmacSHA256", jwtSecret.getKey()).hmacHex(tokenWithoutSignature);
            if (signature.equals(expectedSignature)) {
                LOGGER.info("Token is valid");
                final HasuraJwtPayload hasuraJwtPayload =
                        objectMapper.readValue(Base64.getUrlDecoder().decode(payload),
                                HasuraJwtPayload.class);
                if (!hasuraJwtPayload.getIss().equals(jwtSecret.getIssuer())) {

                }

                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return hasuraJwtPayload;
                    }

                    @Override
                    public Object getDetails() {
                        return hasuraJwtPayload.getClaims();
                    }

                    @Override
                    public Object getPrincipal() {
                        return hasuraJwtPayload.getSub();
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return true;
                    }

                    @Override
                    public void setAuthenticated(boolean b) throws IllegalArgumentException {

                    }

                    @Override
                    public String getName() {
                        return hasuraJwtPayload.getSub();
                    }
                };
            }
        } catch (Exception exception) {
            return unauthenticated();
        }
        return unauthenticated();
    }

    private Authentication unauthenticated() {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean b) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return null;
            }
        };
    }
}
