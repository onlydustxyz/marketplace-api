package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustGrantedAuthority;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtHeader;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;

@AllArgsConstructor
@Slf4j
public class HasuraJwtService implements JwtService {

  private final ObjectMapper objectMapper;
  private final JwtSecret jwtSecret;
  private final UserFacadePort userFacadePort;

  private User getUserFromClaims(HasuraJwtPayload.HasuraClaims claims) {
    final Long githubUserId = claims.getGithubUserId();
    return this.userFacadePort.getUserByGithubIdentity(GithubUserIdentity.builder()
        .githubUserId(githubUserId)
        .githubLogin(claims.getLogin())
        .githubAvatarUrl(claims.getAvatarUrl())
        .build(), false);
  }

  public Optional<OnlyDustAuthentication> getAuthenticationFromJwt(final String jwt,
      final String impersonationHeader) {
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

    User user = getUserFromClaims(hasuraJwtPayload.getClaims());

    if (impersonationHeader != null && !impersonationHeader.isEmpty()) {
      return getAuthenticationFromImpersonationHeader(hasuraJwtPayload, user, impersonationHeader, jwt);
    }

    return Optional.of(HasuraAuthentication.builder()
        .user(user)
        .authorities(user.getRoles().stream().map(OnlyDustGrantedAuthority::new).collect(Collectors.toList()))
        .credentials(hasuraJwtPayload)
        .isAuthenticated(true)
        .claims(hasuraJwtPayload.getClaims())
        .principal(user.getGithubLogin())
        .impersonating(false)
        .jwt(jwt)
        .build());
  }

  private Optional<OnlyDustAuthentication> getAuthenticationFromImpersonationHeader(HasuraJwtPayload hasuraJwtPayload, User impersonator,
      final String impersonationHeader, final String jwt) {

    if (!impersonator.getRoles().contains(UserRole.ADMIN)) {
      LOGGER.warn("User {} is not allowed to impersonate", impersonator.getGithubLogin());
      return Optional.empty();
    }
    final HasuraJwtPayload.HasuraClaims claims;
    try {
      claims = objectMapper.readValue(impersonationHeader, HasuraJwtPayload.HasuraClaims.class);
    } catch (JsonProcessingException e) {
      LOGGER.warn("Invalid impersonation header: {}", impersonationHeader);
      return Optional.empty();
    }

    final User impersonated = getUserFromClaims(claims);

    LOGGER.info("User {} is impersonating {}", impersonator, impersonated);

    return Optional.of(HasuraAuthentication.builder()
        .user(impersonated)
        .authorities(impersonated.getRoles().stream().map(OnlyDustGrantedAuthority::new).collect(Collectors.toList()))
        .credentials(hasuraJwtPayload)
        .isAuthenticated(true)
        .claims(claims)
        .principal(impersonated.getGithubLogin())
        .impersonating(true)
        .impersonator(impersonator)
        .jwt(jwt)
        .impersonationHeader(impersonationHeader)
        .build());
  }
}
