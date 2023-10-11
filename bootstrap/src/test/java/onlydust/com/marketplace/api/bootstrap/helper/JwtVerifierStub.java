package onlydust.com.marketplace.api.bootstrap.helper;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.Setter;

public class JwtVerifierStub implements JWTVerifier {

    @Setter
    private DecodedJWT decodedJWT;
    
    @Override
    public DecodedJWT verify(String token) throws JWTVerificationException {
        return decodedJWT;
    }

    @Override
    public DecodedJWT verify(DecodedJWT jwt) throws JWTVerificationException {
        return decodedJWT;
    }
}
