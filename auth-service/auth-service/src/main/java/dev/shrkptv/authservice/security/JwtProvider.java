package dev.shrkptv.authservice.security;

import dev.shrkptv.authservice.database.entity.AuthUser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;


@Slf4j
@Component
public class JwtProvider {
    private SecretKey jwtSecret;
    private Long accessTokenExpireTime;
    private Long refreshTokenExpireTime;


    public JwtProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token.expiration}") Long accessTokenExpireTime,
            @Value("${jwt.refresh-token.expiration}") Long refreshTokenExpireTime
    ){
        this.jwtSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }

    private String generateToken(String login, Long expireTime)
    {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(login)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expireTime)))
                .signWith(jwtSecret)
                .compact();
    }

    public String generateRefreshToken(String login){
        return generateToken(login, refreshTokenExpireTime);
    }

    public String generateAccessToken(String login){
        return generateToken(login, accessTokenExpireTime);
    }

    private boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Token expired: ", expEx);
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt: ", unsEx);
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt: ", mjEx);
        } catch (SignatureException sEx) {
            log.error("Invalid signature: ", sEx);
        } catch (Exception e) {
            log.error("invalid token: ", e);
        }
        return false;
    }

    public boolean validateRefreshToken(String refreshToken){
        return validateToken(refreshToken);
    }

    public boolean validateAccessToken(String accessToken){
        return validateToken(accessToken);
    }

    public String getLoginFromToken(String token){
        return Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
