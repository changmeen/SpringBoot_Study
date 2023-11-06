package rcm.rcmarket.service.sign;

import rcm.rcmarket.handler.JwtHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

// MockitoExtention을 사용
// TokenService는 JwtHandler에 의존성을 가지고 있음 하지만 테스트이기에 Mockito를 써서 가짜 객체 생성함
// Mockito의 given 함수는 가짜 객체의 행위가 반환해야할 데이터를 미리 준비하여 주입할 수 있음
// Mockito의 verify 함수를 사용하면 가짜 객체가 수행한 행위의 검증도 가능하다
@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
    @InjectMocks TokenService tokenService;
    @Mock JwtHandler jwtHandler;

    // ReflectionTestUtils를 사용하면 setter 메소드를 사용하지 않고 어떠한 객체의 필드값을 임의로 주입해줄 수 있다.
    @BeforeEach
    void beforeEach() {
        ReflectionTestUtils.setField(tokenService, "accessTokenMaxAgeSeconds", 10L);
        ReflectionTestUtils.setField(tokenService, "refreshTokenMaxAgeSeconds", 10L);
        ReflectionTestUtils.setField(tokenService, "accessKey", "accessKey");
        ReflectionTestUtils.setField(tokenService, "refreshKey", "refreshKey");
    }

    // given을 통해 TokenService가 의존하고 있는 가짜 객체의 행위를 지정해주고
    // 이에 대한 반환 값의 메소드로, 이 객체의 행위가 반환해야 할 데이터를 willReturn을 통해 지정해준다.
    // 즉 jwtHandler가 createToken을 하면 3가지의 변수가 들어가고 access라는 값을 반환한다.
    @Test
    void createAccessTokenTest() {
        // given
        given(jwtHandler.createToken(anyString(), anyString(), anyLong())).willReturn("access");

        // when
        String token = tokenService.createAccessToken("subject");

        // then
        assertThat(token).isEqualTo("access");
        verify(jwtHandler).createToken(anyString(), anyString(), anyLong());
    }

    @Test
    void createRefreshTokenTest() {
        // given
        given(jwtHandler.createToken(anyString(), anyString(), anyLong())).willReturn("refresh");

        // when
        String token = tokenService.createRefreshToken("subject");

        // then
        assertThat(token).isEqualTo("refresh");
        verify(jwtHandler).createToken(anyString(), anyString(), anyLong());
    }

    @Test
    void validateAccessTokenTest() {
        // given
        given(jwtHandler.validate(anyString(), anyString())).willReturn(true);

        // when, then
        assertThat(tokenService.validateAccessToken("token")).isTrue();
    }

    @Test
    void invalidateAccessTokenTest() {
        // given
        given(jwtHandler.validate(anyString(), anyString())).willReturn(false);

        // when, then
        assertThat(tokenService.validateAccessToken("token")).isFalse();
    }

    @Test
    void validateRefreshTokenTest() {
        // given
        given(jwtHandler.validate(anyString(), anyString())).willReturn(true);

        // when, then
        assertThat(tokenService.validateRefreshToken("token")).isTrue();
    }

    @Test
    void invalidateRefreshTokenTest() {
        // given
        given(jwtHandler.validate(anyString(), anyString())).willReturn(false);

        // when, then
        assertThat(tokenService.validateRefreshToken("token")).isFalse();
    }

    @Test
    void extractAccessTokenSubjectTest() {
        // given
        String subject = "subject";
        given(jwtHandler.extractSubject(anyString(), anyString())).willReturn(subject);

        // when
        String result = tokenService.extractAccessTokenSubject("token");

        // then
        assertThat(subject).isEqualTo(result);
    }

    @Test
    void extractRefreshTokenSubjectTest() {
        // given
        String subject = "subject";
        given(jwtHandler.extractSubject(anyString(), anyString())).willReturn(subject);

        // when
        String result = tokenService.extractRefreshTokenSubject("token");

        // then
        assertThat(subject).isEqualTo(result);
    }
}
