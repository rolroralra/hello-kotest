package com.example.hellokotest.assertions

import com.example.hellokotest.member.Member
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeBetween
import io.kotest.matchers.ints.shouldBeEven
import io.kotest.matchers.ints.shouldBePositive
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.beBlank
import io.kotest.matchers.string.shouldBeBlank
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.string.startWith
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * 05-assertions.md
 */
class BasicMatchersTest : FunSpec({

    context("동등성") {
        test("shouldBe / shouldNotBe") {
            (1 + 1) shouldBe 2
            "kotlin" shouldNotBe "java"
        }

        test("data class 는 equals 로 비교한다") {
            Member(1, "홍길동", "hong@example.com") shouldBe Member(1, "홍길동", "hong@example.com")
        }
    }

    context("문자열") {
        val greeting = "Hello, Kotest!"

        test("자주 쓰는 문자열 매처") {
            greeting shouldStartWith "Hello"
            greeting shouldEndWith "!"
            greeting shouldContain "Kotest"
            greeting shouldHaveLength 14
            "   ".shouldBeBlank()
        }

        test("정규식") {
            "010-1234-5678" shouldMatch Regex("""\d{3}-\d{4}-\d{4}""")
        }

        test("should / shouldNot 과 매처 함수를 조합할 수도 있다") {
            greeting should startWith("Hello")
            greeting shouldNot beBlank()
        }
    }

    context("숫자") {
        test("정수 매처") {
            10.shouldBePositive()
            10.shouldBeEven()
            10.shouldBeBetween(1, 100)
            10 shouldBeGreaterThan 5
        }
    }

    context("nullable 과 타입") {
        test("null 검사") {
            val nothing: String? = null
            nothing.shouldBeNull()
        }

        test("shouldNotBeNull 은 이후 코드에서 non-null 타입으로 스마트 캐스트 된다") {
            val name: String? = "kotest"
            name.shouldNotBeNull()
            name.length shouldBe 6
        }

        test("shouldBeInstanceOf 는 캐스트된 값을 돌려준다") {
            val value: Any = "I am a String"
            val str = value.shouldBeInstanceOf<String>()
            str.isNotEmpty().shouldBeTrue()
        }
    }
})
