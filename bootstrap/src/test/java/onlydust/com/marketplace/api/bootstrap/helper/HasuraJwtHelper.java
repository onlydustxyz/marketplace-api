package onlydust.com.marketplace.api.bootstrap.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSigner;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtHeader;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

public class HasuraJwtHelper {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String generateValidJwtFor(final JwtSecret jwtSecret, final HasuraJwtPayload hasuraJwtPayload) throws JsonProcessingException {
        final String header =
                Base64.getUrlEncoder().encodeToString(OBJECT_MAPPER.writeValueAsBytes(JwtHeader.builder().alg("HS256").build()));
        final String payload = Base64.getUrlEncoder().encodeToString(OBJECT_MAPPER.writeValueAsBytes(hasuraJwtPayload));
        final String headerAndPayload = header + "." + payload;
        final SignatureAlgorithm sa = HS256;
        final SecretKeySpec secretKeySpec = new SecretKeySpec(
                jwtSecret.getKey().getBytes(), sa.getJcaName());
        final DefaultJwtSigner defaultJwtSigner = new DefaultJwtSigner(sa, secretKeySpec);

        return headerAndPayload + "." + defaultJwtSigner.sign(headerAndPayload);
    }

    public static String generateValidJwtFor(final JwtSecret jwtSecret, final String payloadStr) throws JsonProcessingException {
        final String header =
                Base64.getUrlEncoder().encodeToString(OBJECT_MAPPER.writeValueAsBytes(JwtHeader.builder().alg("HS256").build()));
        final String payload = Base64.getUrlEncoder().encodeToString(payloadStr.getBytes());
        final String headerAndPayload = header + "." + payload;
        final SignatureAlgorithm sa = HS256;
        final SecretKeySpec secretKeySpec = new SecretKeySpec(
                jwtSecret.getKey().getBytes(), sa.getJcaName());
        final DefaultJwtSigner defaultJwtSigner = new DefaultJwtSigner(sa, secretKeySpec);
        return headerAndPayload + "." + defaultJwtSigner.sign(headerAndPayload);
    }


}
