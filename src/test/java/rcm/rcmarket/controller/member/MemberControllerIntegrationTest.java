package rcm.rcmarket.controller.member;

import rcm.rcmarket.dto.sign.SignInRequest;
import rcm.rcmarket.dto.sign.SignInResponse;
import rcm.rcmarket.entity.member.Member;
import rcm.rcmarket.exception.MemberNotFoundException;
import rcm.rcmarket.init.TestInitDB;
import rcm.rcmarket.repository.member.MemberRepository;
import rcm.rcmarket.service.sign.SignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Spring Security와 관한 테스트에는 @SpringBootTest를 쓴다
// SpringBootTest의 기본 웹 관련 설정은 WebEnvironment.MOCK인데
// 내장 톰켓을 실제로 띄워서 하고 싶으면 webEnvironment 설정을 RANDOM_PORT로 설정하면 된다
@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// 우리는 initDB의 프로필을 Local로 해두었는데 이러면 initDB가 빈으로 등록되어
// 테스트에 필요한 데이터와는 다른 데이터가 들어올 수 있으니 test로 쓴다
@ActiveProfiles(value = "test")
// DB에서 입출력이 일어나니 Transactional 사용
@Transactional
public class MemberControllerIntegrationTest {
    // MockMvc를 빌드하기 위해서 WebApplicationContext가 필요하다 함
    @Autowired WebApplicationContext context;
    @Autowired MockMvc mockMvc;

    @Autowired TestInitDB initDB;
    @Autowired SignService signService;
    @Autowired MemberRepository memberRepository;

    @BeforeEach
    void beforeEach() {
        // apply는 Spring Security를 활성화 하기위해 호출하는 것이다
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        initDB.initDB();
    }

    // SecurityConfig에서 GET요청은 모두 permitAll 상태다
    @Test
    void readTest() throws Exception {
        // given
        Member member = memberRepository.findByEmail(initDB.getMember1Email()).orElseThrow(MemberNotFoundException::new);

        // when, then
        mockMvc.perform(
                get("/api/members/{id}", member.getId()))
                .andExpect(status().isOk());
    }

    // 로그인하며 발급받은 엑세스 토큰을 Authorization 헤더에 포함하여 요청을 보내면 정상적으로 delete가 수행된다.
    @Test
    void deleteTest() throws Exception {
        // given
        Member member = memberRepository.findByEmail(initDB.getMember1Email()).orElseThrow(MemberNotFoundException::new);
        SignInResponse adminSignRes = signService.signIn(new SignInRequest(initDB.getMember1Email(), initDB.getPassword()));

        // when, then
        mockMvc.perform(
                delete("/api/members/{id}", member.getId()).header("Authorization", adminSignRes.getAccessToken()))
                .andExpect(status().isOk());
    }

    // 관리자가 로그인해서 발급받은 토큰으로 다른 사용자(member1)의 정보를 삭제할 수 있다.
    @Test
    void deleteByAdminTest() throws Exception {
        // given
        Member member = memberRepository.findByEmail(initDB.getMember1Email()).orElseThrow(MemberNotFoundException::new);
        SignInResponse adminSignRes = signService.signIn(new SignInRequest(initDB.getAdminEmail(), initDB.getPassword()));

        // when, then
        mockMvc.perform(
                delete("/api/members/{id}", member.getId()).header("Authorization", adminSignRes.getAccessToken()))
                .andExpect(status().isOk());
    }

    // 인증받지 않은 사용자가 요청(엑세스 토큰이 Authorization 헤더에 담겨있지 않음)은 거부되고
    // 지정해두었던 CustomAuthenticationEntryPoint가 작동해 3xx 상태 코드를 응답받아
    // /exception/entry-point로 리다이렉트 된다.
    @Test
    void deleteUnauthorizedByNoneTokenTest() throws Exception {
        // given
        Member member = memberRepository.findByEmail(initDB.getMember1Email()).orElseThrow(MemberNotFoundException::new);

        // when, then
        mockMvc.perform(
                delete("/api/members/{id}", member.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }

    // 인증된 사용자지만 자신의 정보(자원)이 아닌 다른 사람의 정보(자원)에 접근하는 요청을 한 상황
    // 관리자가 아닌 일반 사용자 ROLE_NORMAL이기에 요청을 수행할 권한이 없고
    // CustomAccessDeniedHandler에 의해 3xx 상태 코드를 받아 /exception/access-denied로 리다이렉트 된다.
    @Test
    void deleteAccessDeniedByNotResourceOwnerTest() throws Exception {
        // given
        Member member = memberRepository.findByEmail(initDB.getMember1Email()).orElseThrow(MemberNotFoundException::new);
        SignInResponse attackerSignInRes = signService.signIn(new SignInRequest(initDB.getMember2Email(), initDB.getPassword()));

        // when, then
        mockMvc.perform(
                delete("/api/memebers/{id}", member.getId()).header("Authorization", attackerSignInRes.getAccessToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/access-denied"));
    }

    // API 요청은 AccessToken만 가능하게 되어있다(어디에 되어 있는지는 잘 모르겠음)
    // 그런데 RefreshToken을 통해 요청하고 있기에 요청이 거부되고
    // CustomAccessDeinedHandler가 작동하여 3xx 상태 코드를 응답받고
    // /exception/access-denied로 리다이렉트 된다.
    @Test
    void deleteAccessDeniedByRefreshTokenTest() throws Exception {
        // given
        Member member = memberRepository.findByEmail(initDB.getMember1Email()).orElseThrow(MemberNotFoundException::new);
        SignInResponse signInRes = signService.signIn(new SignInRequest(initDB.getMember1Email(), initDB.getPassword()));

        // when, then
        mockMvc.perform(
                delete("/api/members/{id}", member.getId()).header("Authorization", signInRes.getRefreshToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/access-denied"));
    }
}
