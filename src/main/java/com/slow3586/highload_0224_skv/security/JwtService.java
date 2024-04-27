package com.slow3586.highload_0224_skv.security;

import com.slow3586.highload_0224_skv.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${app.jwtService.key}")
    private String jwtSigningKey;

    public UUID extractUserId(String token) {
        final String s = extractClaim(token, Claims::getSubject);
        return StringUtils.isBlank(s) ? null : UUID.fromString(s);
    }

    public String generateToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getUsername());
        return Jwts.builder().setClaims(claims).setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 100000 * 60 * 24))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public boolean isTokenValid(String token, UserEntity userEntity) {
        final UUID userName = extractUserId(token);
        return (userName.equals(userEntity.getId())) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
