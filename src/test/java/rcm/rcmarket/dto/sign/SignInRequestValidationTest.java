package rcm.rcmarket.dto.sign;

import org.junit.jupiter.api.Test;
import rcm.rcmarket.dto.sign.SignInRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class SignInRequestValidationTest {

    // 검증 작업을 수행하기 위해 Validator를 빌드한다
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // 정상적인 요청 객체를 생성하는 팩토리 메소드
    private SignInRequest createRequest() {
        return new SignInRequest("email@email.com", "123456a!");
    }

    // 전달받은 email 필드 외에는 정상적인 요청 객체를 생성하는 팩토리 메소드
    private SignInRequest createRequestWithEmail(String email) {
        return new SignInRequest(email, "123456a!");
    }

    // 전달받은 password 필드 외에는 정상적인 요청 객체를 생성하는 팩토리 메소드
    private SignInRequest createRequestWithPassword(String password) {
        return new SignInRequest("email@email.com", password);
    }

    // 만약 validate에 문제가 없다면 위반 내용이 반환되지 않을 겻이고 isEmpty일 것이다.
    // 하지만 위반 내용이 있다면 위반 내용이 반환되어 오기에 isNotEmpty일 것이다.
    @Test
    void validateTest() {
        // given
        SignInRequest req = createRequest();

        // when
        // 검증을 수행하고 제약 조건을 위반한 내용들을 응답 결과를 받아낸다
        Set<ConstraintViolation<SignInRequest>> validate = validator.validate(req);

        // then
        // 제약 조건이 모두 지켜질 경우 응답 결과는 비어있다.
        assertThat(validate).isEmpty();
    }

    @Test
    void invalidateByNotFormattedEmailTest() {
        // given
        String invalidValue = "email";
        SignInRequest req = createRequestWithEmail(invalidValue);

        // when
        Set<ConstraintViolation<SignInRequest>> validate = validator.validate(req);

        // then
        // 위반 내용이 있기에 isNotEmpty다
        assertThat(validate).isNotEmpty();
        // 위반 내용을 꺼내서 given에서 설정해두었던 위반된 값을 가지고 있는지 확인함
        assertThat(validate.stream().map(v -> v.getInvalidValue()).collect(toSet())).contains(invalidValue);
    }

    @Test
    void invalidateByEmptyEmailTest() {
        // given
        String invalidValue = null;
        SignInRequest req = createRequestWithEmail(invalidValue);

        // when
        Set<ConstraintViolation<SignInRequest>> validate = validator.validate(req);

        // then
        assertThat(validate).isNotEmpty();
        assertThat(validate.stream().map(v -> v.getInvalidValue()).collect(toSet())).contains(invalidValue);
    }

    @Test
    void invalidateByBlankEmailTest() {
        // given
        String invalidValue = " ";
        SignInRequest req = createRequestWithEmail(invalidValue);

        // when
        Set<ConstraintViolation<SignInRequest>> validate = validator.validate(req);

        // then
        assertThat(validate).isNotEmpty();
        assertThat(validate.stream().map(v -> v.getInvalidValue()).collect(toSet())).contains(invalidValue);
    }

    @Test
    void invalidateByEmptyPasswordTest() {
        // given
        String invalidValue = null;
        SignInRequest req = createRequestWithPassword(invalidValue);

        // when
        Set<ConstraintViolation<SignInRequest>> validate = validator.validate(req);

        // then
        assertThat(validate).isNotEmpty();
        assertThat(validate.stream().map(v -> v.getInvalidValue()).collect(toSet())).contains(invalidValue);
    }

    @Test
    void invalidateByBlankPasswordTest() {
        // given
        String invalidValue = null;
        SignInRequest req = createRequestWithPassword(invalidValue);

        // when
        Set<ConstraintViolation<SignInRequest>> validate = validator.validate(req);

        // then
        assertThat(validate).isNotEmpty();
        assertThat(validate.stream().map(v -> v.getInvalidValue()).collect(toSet())).contains(invalidValue);
    }
}
