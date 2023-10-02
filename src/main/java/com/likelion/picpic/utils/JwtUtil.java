package com.likelion.picpic.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {  //토큰생성유틸
    public static String createJwt(String email, String secretKey, Long expiredMs){
        Claims claims= Jwts.claims();
        claims.put("email", email);  //여기서 "email"은 키 느낌
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    
    public static boolean isExpired(String token, String secretKey){ //토큰 기간 점검
       return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().
               getExpiration().before(new Date()); 
    }
    
    public static String getEmail(String token, String secretKey){  //토큰에서 userName꺼내기
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("email", String.class);  //"email"이라는 키로 꺼내기
    }
}
