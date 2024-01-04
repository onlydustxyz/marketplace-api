package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.SneakyThrows;

public class Auth0JwtVerifier implements JWTVerifier {

  private final JWTVerifier jwtVerifier;

  public Auth0JwtVerifier(final Auth0Properties conf) {
    JwkProvider provider = new JwkProviderBuilder(conf.getJwksUrl())
        .build();

    RSAKeyProvider keyProvider = new RSAKeyProvider() {
      @SneakyThrows
      @Override
      public RSAPublicKey getPublicKeyById(String kid) {
        return (RSAPublicKey) provider.get(kid).getPublicKey();
      }

      @Override
      public RSAPrivateKey getPrivateKey() {
        // return the private key used
        return null;
      }

      @Override
      public String getPrivateKeyId() {
        return null;
      }
    };
    Algorithm algorithm = Algorithm.RSA256(keyProvider);

    this.jwtVerifier = JWT.require(algorithm)
        .acceptExpiresAt(conf.getExpiresAtLeeway())
        .withClaimPresence("nickname")
        .withClaimPresence("picture")
        .build();
  }

  @Override
  public DecodedJWT verify(String token) throws JWTVerificationException {
    return jwtVerifier.verify(token);
  }

  @Override
  public DecodedJWT verify(DecodedJWT jwt) throws JWTVerificationException {
    return jwtVerifier.verify(jwt);
  }
}
