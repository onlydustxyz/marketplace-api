package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJWTDecoded;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Slf4j
@AllArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final HasuraProperties hasuraProperties;

    private final AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        LOGGER.info(authorization);
        if (authorization.startsWith("Bearer ")) {
            final String[] chunks = authorization.replace("Bearer ", "").split("\\.");
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
                if (!hasuraJWTDecoded.getIss().equals(jwtSecret.issuer)) {

                }


                SecurityContextHolder.getContext().setAuthentication(new Authentication() {
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
                });


            }


        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }


}
