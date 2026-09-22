package com.altis.library.auth.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String email) throws JOSEException {
        Instant issuedAt = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(issuedAt.plusSeconds(3600)))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        signedJWT.sign(new MACSigner(secretKey.getEncoded()));

        return signedJWT.serialize();
    }

    public String validateToken(String token) {
        if (token == null) {
            return null;
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) {
                return null;
            }

            if (!signedJWT.verify(new MACVerifier(secretKey.getEncoded()))) {
                return null;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();

            if (expirationTime == null || !expirationTime.after(new Date())) {
                return null;
            }

            return claims.getSubject();
        } catch (JOSEException | ParseException exception) {
            return null;
        }
    }
}
