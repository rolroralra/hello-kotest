package com.example.hellokotest.mocking

import com.example.hellokotest.member.DuplicateEmailException
import com.example.hellokotest.member.Member
import com.example.hellokotest.member.MemberNotFoundException
import com.example.hellokotest.member.MemberRepository
import com.example.hellokotest.member.MemberService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

/**
 * 10-mocking-with-mockk.md
 * Spring 없이 MockK 만으로 서비스 로직을 단위 테스트한다.
 */
class MemberServiceMockkTest : BehaviorSpec({

    val memberRepository = mockk<MemberRepository>()
    val memberService = MemberService(memberRepository)

    // 테스트 간 stub / 호출 기록이 섞이지 않도록 매 테스트 후 초기화한다.
    afterEach { clearMocks(memberRepository) }

    Given("회원 가입") {
        When("사용하지 않은 이메일이면") {
            Then("이메일을 정규화해서 저장한다") {
                val nameSlot = slot<String>()
                val emailSlot = slot<String>()
                every { memberRepository.findByEmail(any()) } returns null
                every { memberRepository.save(capture(nameSlot), capture(emailSlot)) } answers {
                    Member(1L, nameSlot.captured, emailSlot.captured)
                }

                val member = memberService.register("  홍길동 ", " Hong@Example.COM ")

                member shouldBe Member(1L, "홍길동", "hong@example.com")
                emailSlot.captured shouldBe "hong@example.com"
                verify(exactly = 1) { memberRepository.save("홍길동", "hong@example.com") }
            }
        }

        When("이미 사용 중인 이메일이면") {
            Then("DuplicateEmailException 이 발생하고 저장하지 않는다") {
                every { memberRepository.findByEmail("hong@example.com") } returns
                    Member(1L, "홍길동", "hong@example.com")

                shouldThrow<DuplicateEmailException> {
                    memberService.register("다른사람", "hong@example.com")
                }
                verify(exactly = 0) { memberRepository.save(any(), any()) }
            }
        }
    }

    Given("회원 조회") {
        When("존재하는 id 면") {
            Then("회원을 돌려준다") {
                every { memberRepository.findById(1L) } returns Member(1L, "홍길동", "hong@example.com")

                memberService.getMember(1L).name shouldBe "홍길동"
            }
        }

        When("존재하지 않는 id 면") {
            Then("MemberNotFoundException 이 발생한다") {
                every { memberRepository.findById(any()) } returns null

                val exception = shouldThrow<MemberNotFoundException> { memberService.getMember(99L) }
                exception.message shouldBe "회원을 찾을 수 없습니다. id=99"
            }
        }
    }

    Given("relaxed mock") {
        Then("stub 하지 않은 함수도 기본값을 돌려준다") {
            val relaxed = mockk<MemberRepository>(relaxed = true)

            MemberService(relaxed).getMembers() shouldBe emptyList()
        }
    }
})
