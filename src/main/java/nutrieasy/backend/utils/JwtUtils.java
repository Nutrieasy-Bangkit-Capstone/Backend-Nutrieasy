package nutrieasy.backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * Created by Resa S.
 * Date: 08-05-2024
 * Created in IntelliJ IDEA.
 */
public class JwtUtils {
    private static final String SECRET = "C4P5T0N3_B4N6K1T";
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public static String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
