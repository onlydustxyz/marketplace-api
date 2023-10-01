package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@AllArgsConstructor
@Slf4j
public class HasuraJwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final HasuraProperties hasuraProperties;

    public Authentication getAuthenticationFromJWT(final String authorizationBearer) throws IOException {
        try {
            final String[] chunks = authorizationBearer.replace(BEARER_PREFIX, "").split("\\.");
            final String payload = chunks[1];
            final String header = chunks[0];
            String tokenWithoutSignature = header + "." + payload;
            String signature = chunks[2];

            final JwtSecret jwtSecret = objectMapper.readValue(hasuraProperties.getSecret(), JwtSecret.class);

            if (!Base64.getUrlDecoder().decode(header).equals("HS256")) {

            }
            SignatureAlgorithm sa = HS256;
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    jwtSecret.getKey().getBytes(), sa.getJcaName());

            DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
            if (validator.isValid(tokenWithoutSignature, signature)) {
                LOGGER.info("Token is valid");
                final HasuraJWTDecoded hasuraJWTDecoded =
                        objectMapper.readValue(Base64.getUrlDecoder().decode(payload),
                                HasuraJWTDecoded.class);
                if (!hasuraJWTDecoded.getIss().equals(jwtSecret.getIssuer())) {

                }

                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return hasuraJWTDecoded;
                    }

                    @Override
                    public Object getDetails() {
                        return hasuraJWTDecoded.getClaims();
                    }

                    @Override
                    public Object getPrincipal() {
                        return hasuraJWTDecoded.getSub();
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
                        return hasuraJWTDecoded.getSub();
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
