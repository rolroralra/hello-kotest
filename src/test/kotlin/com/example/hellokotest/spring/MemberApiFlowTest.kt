package com.example.hellokotest.spring

import com.example.hellokotest.member.InMemoryMemberRepository
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/**
 * 12-spring-boot-integration.md - 전체 컨텍스트 + MockMvc 로 API 흐름 검증
 * 가입 -> 조회 -> 중복 가입 실패 시나리오를 BehaviorSpec 으로 표현한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MemberApiFlowTest(
    private val mockMvc: MockMvc,
    private val memberRepository: InMemoryMemberRepository,
) : BehaviorSpec({

    beforeSpec { memberRepository.clear() }

    Given("새로운 회원이") {
        val body = """{"name": "홍길동", "email": "hong@example.com"}"""

        When("가입하면") {
            Then("201 Created 와 id 1 을 받는다") {
                mockMvc.post("/api/members") {
                    contentType = MediaType.APPLICATION_JSON
                    content = body
                }.andExpect {
                    status { isCreated() }
                    jsonPath("$.data.id") { value(1) }
                }
            }
        }

        When("가입 후 목록을 조회하면") {
            Then("1명이 조회된다") {
                mockMvc.get("/api/members").andExpect {
                    status { isOk() }
                    jsonPath("$.data.length()") { value(1) }
                    jsonPath("$.data[0].name") { value("홍길동") }
                }
            }
        }

        When("같은 이메일로 다시 가입하면") {
            Then("409 Conflict 를 받는다") {
                mockMvc.post("/api/members") {
                    contentType = MediaType.APPLICATION_JSON
                    content = body
                }.andExpect {
                    status { isConflict() }
                    jsonPath("$.success") { value(false) }
                }
            }
        }
    }
})
