package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtHeader;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.apache.commons.codec.digest.HmacUtils;

import java.util.Base64;

public class JwtHelper {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String generateValidJwtFor(final JwtSecret jwtSecret, final HasuraJwtPayload hasuraJwtPayload) throws JsonProcessingException {
        final String header =
                Base64.getUrlEncoder().encodeToString(OBJECT_MAPPER.writeValueAsBytes(JwtHeader.builder().alg("HS256").build()));
        final String payload = Base64.getUrlEncoder().encodeToString(OBJECT_MAPPER.writeValueAsBytes(hasuraJwtPayload));
        final String headerAndPayload = header + "." + payload;
        final String signature = new HmacUtils("HmacSHA256", jwtSecret.getKey()).hmacHex(headerAndPayload);
        return headerAndPayload + "." + signature;
    }

    public static HasuraProperties buildHasuraPropertiesFromSecret(final JwtSecret jwtSecret) throws JsonProcessingException {
        final HasuraProperties hasuraProperties = new HasuraProperties();
        hasuraProperties.setSecret(OBJECT_MAPPER.writeValueAsString(jwtSecret));
        return hasuraProperties;
    }
}
