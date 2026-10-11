package com.example.hellokotest.spring

import com.example.hellokotest.member.Member
import com.example.hellokotest.member.MemberController
import com.example.hellokotest.member.MemberNotFoundException
import com.example.hellokotest.member.MemberService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/**
 * 12-spring-boot-integration.md - @WebMvcTest + @MockkBean
 * 웹 계층(Controller)만 로딩하고, 서비스는 MockK 목 빈으로 대체한다.
 *
 * 필드 주입(@Autowired, @MockkBean)을 사용하기 위해 생성자 람다 대신 init 블록 안에 테스트를 작성한다.
 */
@WebMvcTest(MemberController::class)
class MemberControllerWebMvcTest : DescribeSpec() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberService: MemberService

    init {
        afterEach { clearMocks(memberService) }

        describe("GET /api/members/{id}") {
            it("회원이 있으면 200 과 회원 정보를 돌려준다") {
                every { memberService.getMember(1L) } returns Member(1L, "홍길동", "hong@example.com")

                mockMvc.get("/api/members/1").andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.name") { value("홍길동") }
                    jsonPath("$.data.email") { value("hong@example.com") }
                }
            }

            it("회원이 없으면 404 와 에러 메시지를 돌려준다") {
                every { memberService.getMember(99L) } throws MemberNotFoundException(99L)

                mockMvc.get("/api/members/99").andExpect {
                    status { isNotFound() }
                    jsonPath("$.success") { value(false) }
                    jsonPath("$.error") { value("회원을 찾을 수 없습니다. id=99") }
                }
            }
        }

        describe("POST /api/members") {
            it("요청이 올바르면 201 을 돌려준다") {
                every { memberService.register("홍길동", "hong@example.com") } returns
                    Member(1L, "홍길동", "hong@example.com")

                mockMvc.post("/api/members") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name": "홍길동", "email": "hong@example.com"}"""
                }.andExpect {
                    status { isCreated() }
                    jsonPath("$.data.id") { value(1) }
                }

                verify(exactly = 1) { memberService.register("홍길동", "hong@example.com") }
            }

            it("이메일 형식이 잘못되면 400 을 돌려주고 서비스를 호출하지 않는다") {
                mockMvc.post("/api/members") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name": "홍길동", "email": "not-an-email"}"""
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.success") { value(false) }
                }

                verify(exactly = 0) { memberService.register(any(), any()) }
            }
        }
    }
}
