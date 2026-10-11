package com.example.hellokotest.datadriven

import com.example.hellokotest.basic.Calculator
import com.example.hellokotest.basic.PasswordValidator
import com.example.hellokotest.basic.PasswordValidator.Violation
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

/**
 * 08-data-driven-testing.md
 * JUnit 의 @ParameterizedTest 에 해당한다. 입력값마다 별도의 테스트가 만들어진다.
 */
data class AddCase(val a: Int, val b: Int, val expected: Int)

data class PasswordCase(val password: String, val violation: Violation) : WithDataTestName {
    override fun dataTestName() = "'$password' -> $violation"
}

class DataDrivenTest : FunSpec({

    val calculator = Calculator()

    context("덧셈 - data class 를 데이터로 사용") {
        withData(
            AddCase(1, 1, 2),
            AddCase(-1, 1, 0),
            AddCase(100, 200, 300),
        ) { (a, b, expected) ->
            calculator.add(a, b) shouldBe expected
        }
    }

    context("유효한 비밀번호 - 단순 값 목록") {
        withData("Kotest2026", "Password1", "ABCdef1234") { password ->
            PasswordValidator.validate(password).shouldBeEmpty()
        }
    }

    context("유효하지 않은 비밀번호 - WithDataTestName 으로 테스트 이름 지정") {
        withData(
            PasswordCase("Short1", Violation.TOO_SHORT),
            PasswordCase("NoDigitsHere", Violation.NO_DIGIT),
            PasswordCase("lowercase123", Violation.NO_UPPERCASE),
            PasswordCase("Has Space 123", Violation.CONTAINS_WHITESPACE),
            PasswordCase("A1".repeat(11), Violation.TOO_LONG),
        ) { (password, violation) ->
            PasswordValidator.validate(password) shouldContain violation
        }
    }

    context("나눗셈 - nameFn 으로 테스트 이름 지정") {
        withData(
            nameFn = { (a, b, expected) -> "$a / $b = $expected" },
            Triple(10, 2, 5),
            Triple(9, 3, 3),
            Triple(7, 2, 3),
        ) { (a, b, expected) ->
            calculator.divide(a, b) shouldBe expected
        }
    }

    context("Map 의 key 를 테스트 이름으로 사용") {
        withData(
            mapOf(
                "양수 곱셈" to Triple(2, 3, 6),
                "음수 곱셈" to Triple(-2, 3, -6),
                "0 곱셈" to Triple(0, 100, 0),
            ),
        ) { (a, b, expected) ->
            calculator.multiply(a, b) shouldBe expected
        }
    }
})
