package com.example.hellokotest.styles

import com.example.hellokotest.basic.PasswordValidator
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * 04-spec-styles.md - DescribeSpec
 * JavaScript(Jest, Mocha) / Ruby(RSpec) 의 describe / it 구조.
 */
class DescribeSpecExample : DescribeSpec({

    describe("PasswordValidator") {
        describe("isValid") {
            it("규칙을 모두 지키면 true") {
                PasswordValidator.isValid("Kotest2026") shouldBe true
            }
            it("숫자가 없으면 false") {
                PasswordValidator.isValid("KotestOnly") shouldBe false
            }
        }
    }
})
