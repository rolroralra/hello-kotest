package com.example.hellokotest.styles

import com.example.hellokotest.basic.Calculator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * 04-spec-styles.md - FunSpec
 * JUnit 과 가장 비슷한 스타일. context 로 그룹을 만들 수 있다.
 */
class FunSpecExample : FunSpec({

    val calculator = Calculator()

    test("덧셈") {
        calculator.add(2, 3) shouldBe 5
    }

    context("나눗셈") {
        test("나누어 떨어지는 경우") {
            calculator.divide(10, 5) shouldBe 2
        }
        test("나머지는 버린다") {
            calculator.divide(7, 2) shouldBe 3
        }
    }
})
