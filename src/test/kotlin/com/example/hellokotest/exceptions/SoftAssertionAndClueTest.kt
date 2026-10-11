package com.example.hellokotest.exceptions

import com.example.hellokotest.basic.Money
import com.example.hellokotest.member.Member
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith

/**
 * 06-exceptions-and-soft-assertions.md - Soft Assertion, Clue, 커스텀 매처
 */

// 커스텀 매처: 금액이 특정 통화인지 검사
fun haveCurrency(currency: String) = Matcher<Money> { value ->
    MatcherResult(
        value.currency == currency,
        { "통화가 $currency 이어야 하지만 ${value.currency} 입니다." },
        { "통화가 $currency 가 아니어야 합니다." },
    )
}

infix fun Money.shouldHaveCurrency(currency: String): Money {
    this should haveCurrency(currency)
    return this
}

class SoftAssertionAndClueTest : FunSpec({

    val member = Member(1, "홍길동", "hong@example.com")

    test("assertSoftly - 실패해도 멈추지 않고 모든 검증을 실행한 뒤 한꺼번에 보고한다") {
        assertSoftly(member) {
            id shouldBe 1
            name shouldBe "홍길동"
            email shouldEndWith "@example.com"
        }
    }

    test("assertSoftly 는 여러 실패를 모아서 보여준다") {
        val error = shouldThrow<AssertionError> {
            assertSoftly {
                member.name shouldBe "임꺽정"
                member.email shouldBe "lim@example.com"
            }
        }
        error.message shouldContain "The following 2 assertions failed"
    }

    test("withClue - 실패 메시지에 맥락을 덧붙인다") {
        withClue("회원 이름 검증") {
            member.name shouldBe "홍길동"
        }

        val error = shouldThrow<AssertionError> {
            withClue("id=${member.id} 회원의 이메일") {
                member.email shouldBe "other@example.com"
            }
        }
        error.message shouldContain "id=1 회원의 이메일"
    }

    test("커스텀 매처") {
        val price = Money.of(1000, "krw")

        price should haveCurrency("KRW")
        price shouldNot haveCurrency("USD")
        price shouldHaveCurrency "KRW"
    }

    test("커스텀 매처 실패 메시지") {
        val error = shouldThrow<AssertionError> {
            Money.of(1, "USD") shouldHaveCurrency "KRW"
        }
        error.message shouldBe "통화가 KRW 이어야 하지만 USD 입니다."
    }
})
