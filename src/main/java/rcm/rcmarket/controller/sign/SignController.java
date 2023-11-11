package rcm.rcmarket.controller.sign;

import org.springframework.web.bind.annotation.*;
import rcm.rcmarket.dto.response.Response;
import rcm.rcmarket.dto.sign.SignInRequest;
import rcm.rcmarket.dto.sign.SignUpRequest;
import rcm.rcmarket.service.sign.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import javax.validation.Valid;

import static rcm.rcmarket.dto.response.Response.success;

@RestController // Json으로 응답하기 위해선 RestController를 선언해야 한다
@RequiredArgsConstructor
public class SignController {
    private final SignService signService;

    // 회원가입에 성공하면 201 상태 코드를 응답한다.
    // 요청으로 전달받는 JSON 바디를 객체로 변환하기 위해 @RequestBody를 선언해주고
    // request 객체의 필드 값을 검증하기 위해 @Valid를 선언한다
    @PostMapping("/api/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public Response signUp(@Valid @RequestBody SignUpRequest req) {
        signService.signUp(req);
        return success();
    }

    // 정상적으로 로그인 되면, 200상태코드와 데이터(여기선 Token)을 응답한다
    // signService의 signIn 함수는 SignInResponse를 return하는데
    // SignInResponse는 accessToken과 refresh으로 이루어져 있기 때문이다.
    @PostMapping("/api/sign-in")
    @ResponseStatus(HttpStatus.OK)
    public Response signIn(@Valid @RequestBody SignInRequest req) {
        return success(signService.signIn(req));
    }

    @PostMapping("/api/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public Response refreshToken(@RequestHeader(value = "Authorization") String refreshToken) {
        return success(signService.refreshToken(refreshToken));
    }
}
