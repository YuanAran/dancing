package com.dancing.until;

import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

public class JwtUntil {
    private static final String SECRET="sdahsdasdaosdjaosidjaodnjnkhasdaidhio";
    private static final long EXPIRATION=12*60*60*1000;

    //生成token
    public static String generateToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION))
                .signWith(SignatureAlgorithm.HS256,SECRET.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    //解析token
    public static Claims parseToken(String token) {
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7);
        }
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)  // 解析 JWS，而不是 JWT
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("token 过期");
            return null;
        } catch (SignatureException e) {
            System.out.println("token 签名错误");
            return null;
        } catch (Exception e) {
            System.out.println("token 无效");
            return null;
        }
    }

    // 校验 token 是否有效
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            assert claims != null;
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // 获取用户名
    public static String getUsername(String token) {
        return Objects.requireNonNull(parseToken(token)).getSubject();
    }
}
