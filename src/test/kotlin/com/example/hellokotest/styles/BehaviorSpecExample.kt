package com.example.hellokotest.styles

import com.example.hellokotest.basic.Money
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * 04-spec-styles.md - BehaviorSpec
 * BDD 의 Given / When / Then 구조.
 */
class BehaviorSpecExample : BehaviorSpec({

    Given("지갑에 10,000원이 있을 때") {
        val wallet = Money.of(10_000, "KRW")

        When("3,000원을 사용하면") {
            val remain = wallet - Money.of(3_000, "KRW")

            Then("7,000원이 남는다") {
                remain shouldBe Money.of(7_000, "KRW")
            }
            Then("원래 지갑 객체는 변하지 않는다 (불변 객체)") {
                wallet shouldBe Money.of(10_000, "KRW")
            }
        }

        When("10,000원을 모두 사용하면") {
            val remain = wallet - Money.of(10_000, "KRW")

            Then("잔액은 0원이다") {
                remain.isZero() shouldBe true
            }
        }
    }
})
