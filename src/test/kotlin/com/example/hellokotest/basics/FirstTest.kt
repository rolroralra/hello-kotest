package com.example.hellokotest.basics

import com.example.hellokotest.basic.Calculator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * 03-first-test.md
 * 가장 단순한 형태의 Kotest 테스트. 문자열이 곧 테스트 이름이다.
 */
class FirstTest : StringSpec({

    val calculator = Calculator()

    "1 더하기 2 는 3 이다" {
        calculator.add(1, 2) shouldBe 3
    }

    "5 빼기 3 은 2 이다" {
        calculator.subtract(5, 3) shouldBe 2
    }

    "곱셈과 나눗셈" {
        calculator.multiply(4, 5) shouldBe 20
        calculator.divide(10, 2) shouldBe 5
    }
})
