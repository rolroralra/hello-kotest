package com.example.hellokotest.lifecycle

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/**
 * 07-lifecycle-and-isolation.md - Isolation Mode
 *
 * 기본값(SingleInstance)에서는 Spec 인스턴스 하나를 모든 테스트가 공유한다.
 * 그래서 Spec 본문에 선언한 상태가 테스트 사이에 이어진다.
 */
class SingleInstanceTest : FunSpec({

    val list = mutableListOf<String>()

    test("a") {
        list += "a"
        list shouldContainExactly listOf("a")
    }

    test("b - 이전 테스트에서 추가한 값이 남아 있다") {
        list += "b"
        list shouldContainExactly listOf("a", "b")
    }
})

/**
 * InstancePerLeaf: 말단(leaf) 테스트마다 Spec 을 새로 만들고, 그 경로(Given -> When -> Then)만 다시 실행한다.
 * BehaviorSpec 에서 Given/When 블록의 상태를 Then 마다 깨끗하게 유지하고 싶을 때 유용하다.
 */
class InstancePerLeafTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("빈 장바구니") {
        val cart = mutableListOf<String>()

        When("사과를 담으면") {
            cart += "apple"

            Then("장바구니에 사과 1개") {
                cart shouldContainExactly listOf("apple")
            }
        }

        When("바나나를 담으면") {
            cart += "banana"

            Then("장바구니에 바나나만 있다 (사과 When 블록의 영향을 받지 않는다)") {
                cart shouldContainExactly listOf("banana")
            }
        }

        Then("Given 블록만 실행된 상태이므로 비어 있다") {
            cart.size shouldBe 0
        }
    }
})
