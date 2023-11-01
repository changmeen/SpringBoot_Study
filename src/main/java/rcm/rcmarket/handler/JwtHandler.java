package rcm.rcmarket.handler;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtHandler {

    private String type = "Bearer ";

    // Base64로 인코딩된 Key값을 받고 토큰에 저장될 data인 subject, 만료기간 maxAgeSeconds를
    // 초단위로 입력받아서 토큰을 만들어주는 작업 수행
    public String createToken(String encodeKey, String subject, long maxAgeSeconds) {
        Date now = new Date();

        return type + Jwts.builder()
                .setSubject(subject)    // 토큰에 저장될 데이터 지정
                .setIssuedAt(now)       // 토큰 발급일을 지정 - 현재로 설정
                .setExpiration(new Date(now.getTime() + maxAgeSeconds * 1000L)) // 토큰 만료 일자 지정
                .signWith(SignatureAlgorithm.HS256, encodeKey)  // 파라미터로 받은 Key를 이용해 SHA-256 알고리즘을 적용해 서명
                .compact(); // 토큰 생성
    }

    public String extractSubject(String encodeKey, String token) {
        return parse(encodeKey, token).getBody().getSubject();  // 토큰에서 subject(정보) 추출
    }

    public boolean validate(String encodeKey, String token) {
        try {
            parse(encodeKey, token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Jws<Claims> parse(String key, String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(untype(token));
    }

    private String untype(String token) {
        return token.substring(type.length());
    }

//    parser를 이용하여 사용된 key를 지정해주고, 파싱을 수행해줍니다.
//    이때, 토큰 문자열에는 토큰의 타입도 포함되어있으므로, 이를 untype 메소드를 이용하여 제거해줍니다.
//    JwtHandler는 사용할 때, 기본적으로 Base64로 인코딩된 키를 파라미터로 받게 됩니다.
//    이는, jwt dependency를 이용할 때 인코딩된 키를 인자로 넘겨주어야하기 때문입니다.
//    JwtHandler에서 인코딩되지않은 키를 입력받아서 직접 인코딩한 뒤에 사용해도 되지만,
//    Base64 인코딩은 손쉽게(인코딩사이트 또는 심지어 눈으로든) 할 수 있기 때문에,
//    이를 불필요한 작업이라 보고, 처음부터 인코딩된 키를 넘겨받도록 명시하였습니다.
}
