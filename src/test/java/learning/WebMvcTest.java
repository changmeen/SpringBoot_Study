package learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import rcm.rcmarket.dto.response.Response;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class WebMvcTest {

    @InjectMocks TestController testController;
    MockMvc mockMvc;    // 컨트롤러로 요청을 보내기 위해 MockMvc를 사용한다

    @Controller         // 테스트 용도의 간단한 컨트롤러, Response.success()를 return한다
    public static class TestController {
        @GetMapping("/test/ignore-null-value")
        public Response ignoreNullValueTest() {
            return Response.success();
        }
    }

    // Mockito를 이용해 TestController를 띄워준다. MockMvc로 컨트롤러에 요청을 보내 테스트가 가능함
    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController).build();
    }

    // MockMvc.perform으로 요청을 보내고 결과를 검증한다
    // 응답 코드 상태 200인지 확인, Json에 result 필드가 없음을 확인함
    @Test
    void ignoreNullValueInJsonResponseTest() throws Exception {
        mockMvc.perform(
                get("/test/ignore-null-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").doesNotExist());
    }
}
