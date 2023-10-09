package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtHeader;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.springframework.http.HttpStatus;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Base64;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@AllArgsConstructor
@Slf4j
public class HasuraJwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final JwtSecret jwtSecret;

    public HasuraAuthentication getAuthenticationFromJwt(final String authorizationBearer) {
        final String[] chunks = authorizationBearer.split("\\.");
        if (chunks.length != 3) {
            return getUnauthenticatedWithExceptionFor(HttpStatus.UNAUTHORIZED.value(), "Invalid Jwt format");
        }
        final String payload = chunks[1];
        final String header = chunks[0];
        final String tokenWithoutSignature = header + "." + payload;
        final String signature = chunks[2];

        JwtHeader jwtHeader;
        try {
            jwtHeader = objectMapper.readValue(Base64.getUrlDecoder().decode(header), JwtHeader.class);
        } catch (Exception e) {
            return getUnauthenticatedWithExceptionFor(HttpStatus.UNAUTHORIZED.value(),
                    "Invalid Jwt " + "header format");
        }
        if (!jwtHeader.getAlg().equals("HS256")) {
            return getUnauthenticatedWithExceptionFor(HttpStatus.UNAUTHORIZED.value(), "Invalid Jwt " +
                    "algorithm type");
        }

        final SignatureAlgorithm sa = HS256;
        final SecretKeySpec secretKeySpec = new SecretKeySpec(
                jwtSecret.getKey().getBytes(), sa.getJcaName());


        final DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
        if (validator.isValid(tokenWithoutSignature, signature)) {
            final HasuraJwtPayload hasuraJwtPayload;
            try {
                hasuraJwtPayload = objectMapper.readValue(Base64.getUrlDecoder().decode(payload),
                        HasuraJwtPayload.class);
            } catch (IOException e) {
                return getUnauthenticatedWithExceptionFor(HttpStatus.UNAUTHORIZED.value(),
                        "Unable to deserialize Jwt token");
            }
            if (!hasuraJwtPayload.getIss().equals(jwtSecret.getIssuer())) {

            }

            return HasuraAuthentication.builder().credentials(hasuraJwtPayload).isAuthenticated(true)
                    .claims(hasuraJwtPayload.getClaims()).principal(hasuraJwtPayload.getSub()).build();
        } else {
            return HasuraAuthentication.builder().isAuthenticated(false)
                    .onlydustException(OnlydustException.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message(String.format("Invalid JWT signature %s", signature))
                            .build()).build();
        }
    }

    private static HasuraAuthentication getUnauthenticatedWithExceptionFor(final int status, final String message) {
        return HasuraAuthentication.builder()
                .onlydustException(OnlydustException
                        .builder()
                        .status(status)
                        .message(message)
                        .build())
                .build();
    }

}
