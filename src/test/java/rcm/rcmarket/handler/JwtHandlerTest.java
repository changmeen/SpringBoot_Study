package rcm.rcmarket.handler;

import org.junit.jupiter.api.Test;

import java.util.Base64;
import static org.assertj.core.api.Assertions.assertThat;

public class JwtHandlerTest {
    JwtHandler jwtHandler = new JwtHandler();

    // Token 생성 test
    @Test
    void createTokenTest() {
        String encodedKey = Base64.getEncoder().encodeToString("myKey".getBytes());
        String token = createToken(encodedKey, "subject", 60L);

        assertThat(token).contains("Bearer");
    }

    @Test
    void extractSubjectTest() {
        String encodedKey = Base64.getEncoder().encodeToString("myKey".getBytes());
        String subject = "subject";
        // 토큰을 만들고
        String token = createToken(encodedKey, subject, 60L);

        // 만들어진 토큰에서 subject를 빼서
        String extractedSubject = jwtHandler.extractSubject(encodedKey, token);

        // 위에서 만든 subject와 extract한 subject가 같은지 확인
        assertThat(extractedSubject).isEqualTo(subject);
    }

    @Test
    void validateTest() {
        String encodedKey = Base64.getEncoder().encodeToString("myKey".getBytes());
        String token = createToken(encodedKey, "subject", 60L);

        boolean isValid = jwtHandler.validate(encodedKey, token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void invalidateByInvalidKeyTest() {
        String encodedKey = Base64.getEncoder().encodeToString("myKey".getBytes());
        String token = createToken(encodedKey, "subject", 60L);

        // 토큰을 사용할때와 다른 key를 사용해 토큰을 검증할 경우 토큰은 유효하지 않음
        boolean isValid = jwtHandler.validate("invalid", token);

        assertThat(isValid).isFalse();
    }

    @Test
    void invalidateByExpiredTokenTest() {
        String encodedKey = Base64.getEncoder().encodeToString("myKey".getBytes());
        // 토큰의 만료기간을 0으로 주어 생성하자마자 동시에 토큰이 만료되게 함
        String token = createToken(encodedKey, "subject", 0L);

        boolean isValid = jwtHandler.validate(encodedKey, token);

        assertThat(isValid).isFalse();
    }

    private String createToken(String encodedKey, String subjecet, long maxAgeSeconds) {
        return jwtHandler.createToken(
                encodedKey,
                subjecet,
                maxAgeSeconds
        );
    }
}
