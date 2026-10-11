package com.example.hellokotest.exceptions

import com.example.hellokotest.basic.Calculator
import com.example.hellokotest.basic.Money
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * 06-exceptions-and-soft-assertions.md - 예외 검증
 */
class ExceptionTest : FunSpec({

    val calculator = Calculator()

    test("shouldThrow 는 발생한 예외를 돌려준다") {
        val exception = shouldThrow<ArithmeticException> {
            calculator.divide(1, 0)
        }
        exception.message shouldBe "0으로 나눌 수 없습니다."
    }

    test("shouldThrowWithMessage 로 메시지까지 한 번에 검증") {
        shouldThrowWithMessage<ArithmeticException>("0으로 나눌 수 없습니다.") {
            calculator.divide(1, 0)
        }
    }

    test("shouldThrow 는 하위 타입도 허용, shouldThrowExactly 는 정확한 타입만 허용") {
        // IllegalArgumentException 은 RuntimeException 의 하위 타입
        shouldThrow<RuntimeException> { Money.of(1000, "WONS") }
        shouldThrowExactly<IllegalArgumentException> { Money.of(1000, "WONS") }
    }

    test("require 실패 메시지 검증") {
        val exception = shouldThrow<IllegalArgumentException> {
            Money.of(1000, "KRW") + Money.of(1, "USD")
        }
        exception.message shouldContain "통화가 다릅니다"
    }

    test("예외가 발생하지 않아야 하는 경우") {
        shouldNotThrowAny {
            calculator.divide(10, 3)
        }
    }
})
