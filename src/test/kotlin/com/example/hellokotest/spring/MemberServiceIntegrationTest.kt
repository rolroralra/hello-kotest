package com.example.hellokotest.spring

import com.example.hellokotest.member.DuplicateEmailException
import com.example.hellokotest.member.InMemoryMemberRepository
import com.example.hellokotest.member.MemberService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

/**
 * 12-spring-boot-integration.md - @SpringBootTest + 생성자 주입
 * ProjectConfig 에 등록한 SpringExtension 덕분에 생성자 파라미터로 빈을 주입받을 수 있다.
 */
@SpringBootTest
class MemberServiceIntegrationTest(
    private val memberService: MemberService,
    private val memberRepository: InMemoryMemberRepository,
) : FunSpec({

    beforeEach { memberRepository.clear() }

    test("실제 빈으로 회원 가입 후 조회") {
        val saved = memberService.register("홍길동", "hong@example.com")

        memberService.getMember(saved.id).email shouldBe "hong@example.com"
        memberService.getMembers() shouldHaveSize 1
    }

    test("중복 이메일은 가입할 수 없다") {
        memberService.register("홍길동", "hong@example.com")

        shouldThrow<DuplicateEmailException> {
            memberService.register("홍길동2", "HONG@example.com")
        }
    }
})
