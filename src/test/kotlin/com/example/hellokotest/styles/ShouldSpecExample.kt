package com.example.hellokotest.styles

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

/**
 * 04-spec-styles.md - ShouldSpec
 */
class ShouldSpecExample : ShouldSpec({

    context("String.reversed()") {
        should("문자열을 뒤집는다") {
            "kotest".reversed() shouldBe "tsetok"
        }
        should("빈 문자열은 그대로 빈 문자열이다") {
            "".reversed() shouldBe ""
        }
    }
})
