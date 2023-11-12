package rcm.rcmarket.handler;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

// JWT는 세 부분으로 나뉜다.
// 헤더, 페이로드, 서명으로 나뉘고
// 헤더는 토큰의 타입, 서명 알고리즘을 포함
// 페이로드는 토큰의 주요 내용
// 서명은 JWT의 무결성을 보장한다.
@Component
public class JwtHandler {

    private String type = "Bearer ";

    // JWT를 생성하는 함수
    // encode: 서명에 사용될 키(Base64로 인코딩 되어있음)
    // JWT를 생성할 때 서명에 사용되는 비밀 값이다
    // subject: 토큰의 주체(subject)로 설정될 값
    // maxAgeSeconds: 토큰의 만료 시간까지의 유효 기간
    // 반환값: 생성된 JWT 문자열
    public String createToken(String encodedKey, String subject, long maxAgeSeconds) {
        Date now = new Date();
        return type + Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + maxAgeSeconds * 1000L))
                .signWith(SignatureAlgorithm.HS256, encodedKey)
                .compact();
    }

    // 주어진 토큰에서 주체(subject)를 추출하는 함수
    // encodedKey: 서명에 사용될 키, 키가 있어야 암, JWT의 무결성을 확인한다.
    // token: 추출할 JWT 토큰
    public String extractSubject(String encodedKey, String token) {
        return parse(encodedKey, token).getBody().getSubject();
    }

    // 주어진 토큰의 유효성을 검증하는 함수
    // encodedKey: 서명에 사용될 키
    // token: 검증할 JWT 토큰
    // 반환값: 유효하면 true, 아니면 false
    public boolean validate(String encodedKey, String token) {
        try {
            parse(encodedKey, token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // JWT에서 파싱은 JWT를 분해하고 그 정보를 추출하는 과정을 의미한다.
    // key: 분해하기 위한 key, createToken에서 사용된 encodedKey가 사용되어야 한다
    // token: 분해되는 JWT token
    // 반환값: JWS(Java Web Signature) 객체, 토큰의 클레임을 포함
    private Jws<Claims> parse(String key, String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(untype(token));
    }

    private String untype(String token) {
        return token.substring(type.length());
    }

}
//    parser를 이용하여 사용된 key를 지정해주고, 파싱을 수행해줍니다.
//    이때, 토큰 문자열에는 토큰의 타입도 포함되어있으므로, 이를 untype 메소드를 이용하여 제거해줍니다.
//    JwtHandler는 사용할 때, 기본적으로 Base64로 인코딩된 키를 파라미터로 받게 됩니다.
//    이는, jwt dependency를 이용할 때 인코딩된 키를 인자로 넘겨주어야하기 때문입니다.
//    JwtHandler에서 인코딩되지않은 키를 입력받아서 직접 인코딩한 뒤에 사용해도 되지만,
//    Base64 인코딩은 손쉽게(인코딩사이트 또는 심지어 눈으로든) 할 수 있기 때문에,
//    이를 불필요한 작업이라 보고, 처음부터 인코딩된 키를 넘겨받도록 명시하였습니다.
