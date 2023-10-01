package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtHeader;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import onlydust.com.marketplace.api.rest.api.adapter.exception.RestApiExceptionCode;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.IOException;
import java.util.Base64;

@AllArgsConstructor
@Slf4j
public class HasuraJwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final JwtSecret jwtSecret;

    public HasuraAuthentication getAuthenticationFromJwt(final String authorizationBearer) {
        final String[] chunks = authorizationBearer.split("\\.");
        if (chunks.length != 3) {
            return getUnauthenticatedWithExceptionFor(RestApiExceptionCode.INVALID_JWT_FORMAT, "Invalid Jwt format");
        }
        final String payload = chunks[1];
        final String header = chunks[0];
        String tokenWithoutSignature = header + "." + payload;
        String signature = chunks[2];

        JwtHeader jwtHeader;
        try {
            jwtHeader = objectMapper.readValue(Base64.getUrlDecoder().decode(header), JwtHeader.class);
        } catch (Exception e) {
            return getUnauthenticatedWithExceptionFor(RestApiExceptionCode.INVALID_JWT_HEADER_FORMAT,
                    "Invalid Jwt " + "header format");
        }
        if (!jwtHeader.getAlg().equals("HS256")) {
            return getUnauthenticatedWithExceptionFor(RestApiExceptionCode.INVALID_JWT_ALGO_TYPE, "Invalid Jwt " +
                    "algorithm type");
        }
        final String expectedSignature = new HmacUtils("HmacSHA256", jwtSecret.getKey()).hmacHex(tokenWithoutSignature);
        if (signature.equals(expectedSignature)) {
            final HasuraJwtPayload hasuraJwtPayload;
            try {
                hasuraJwtPayload = objectMapper.readValue(Base64.getUrlDecoder().decode(payload),
                        HasuraJwtPayload.class);
            } catch (IOException e) {
                return getUnauthenticatedWithExceptionFor(RestApiExceptionCode.UNABLE_TO_DESERIALIZE_JWT_TOKEN,
                        "Unable to deserialize Jwt token");
            }
            if (!hasuraJwtPayload.getIss().equals(jwtSecret.getIssuer())) {

            }

            return HasuraAuthentication.builder().credentials(hasuraJwtPayload).isAuthenticated(true)
                    .claims(hasuraJwtPayload.getClaims()).principal(hasuraJwtPayload.getSub()).build();
        } else {
            return HasuraAuthentication.builder().isAuthenticated(false)
                    .onlydustException(OnlydustException.builder()
                            .code(RestApiExceptionCode.INVALID_JWT_SIGNATURE)
                            .message(String.format("Invalid JWT signature %s", signature))
                            .build()).build();
        }
    }

    private static HasuraAuthentication getUnauthenticatedWithExceptionFor(final String code, final String message) {
        return HasuraAuthentication.builder().onlydustException(OnlydustException.builder().code(code).message(message).build()).build();
    }

}
