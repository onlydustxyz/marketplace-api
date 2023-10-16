package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtHeader;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@AllArgsConstructor
@Slf4j
public class HasuraJwtService implements JwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final JwtSecret jwtSecret;

    public Optional<OnlyDustAuthentication> getAuthenticationFromJwt(final String jwt, final String impersonationHeader) {
        final String[] chunks = jwt.split("\\.");
        if (chunks.length != 3) {
            LOGGER.warn("Invalid Jwt format");
            return Optional.empty();
        }
        final String payload = chunks[1];
        final String header = chunks[0];
        final String tokenWithoutSignature = header + "." + payload;
        final String signature = chunks[2];

        JwtHeader jwtHeader;
        try {
            jwtHeader = objectMapper.readValue(Base64.getUrlDecoder().decode(header), JwtHeader.class);
        } catch (Exception e) {
            LOGGER.warn("Invalid Jwt header format", e);
            return Optional.empty();
        }
        if (!jwtHeader.getAlg().equals("HS256")) {
            LOGGER.warn("Invalid Jwt algorithm type");
            return Optional.empty();
        }

        final SignatureAlgorithm sa = HS256;
        final SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getKey().getBytes(), sa.getJcaName());
        final DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
        if (!validator.isValid(tokenWithoutSignature, signature)) {
            LOGGER.warn("Invalid Jwt signature");
            return Optional.empty();
        }

        final HasuraJwtPayload hasuraJwtPayload;
        try {
            hasuraJwtPayload = objectMapper.readValue(Base64.getUrlDecoder().decode(payload),
                    HasuraJwtPayload.class);
        } catch (IOException e) {
            LOGGER.warn("Unable to deserialize Jwt token", e);
            return Optional.empty();
        }
        if (!hasuraJwtPayload.getIss().equals(jwtSecret.getIssuer())) {
            LOGGER.warn("Invalid Jwt issuer");
            return Optional.empty();
        }

        User user = User.builder()
                .id(hasuraJwtPayload.getClaims().getUserId())
                .githubUserId(hasuraJwtPayload.getClaims().getGithubUserId())
                .permissions(hasuraJwtPayload.getClaims().getAllowedRoles())
                .avatarUrl(hasuraJwtPayload.getClaims().getAvatarUrl())
                .login(hasuraJwtPayload.getClaims().getLogin())
                .build();

        return Optional.of(HasuraAuthentication.builder()
                .user(user)
                .credentials(hasuraJwtPayload)
                .isAuthenticated(true)
                .claims(hasuraJwtPayload.getClaims())
                .principal(hasuraJwtPayload.getSub())
                .build());

    }

}
