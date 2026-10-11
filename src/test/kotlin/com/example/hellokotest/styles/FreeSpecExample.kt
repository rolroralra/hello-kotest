package com.example.hellokotest.styles

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

/**
 * 04-spec-styles.md - FreeSpec
 * `-` 로 원하는 만큼 자유롭게 중첩할 수 있다.
 */
class FreeSpecExample : FreeSpec({

    "List" - {
        "비어 있는 리스트" - {
            val list = emptyList<Int>()

            "size 는 0 이다" {
                list.size shouldBe 0
            }
        }
        "원소가 3개인 리스트" - {
            val list = listOf(1, 2, 3)

            "합계는 6 이다" {
                list.sum() shouldBe 6
            }
        }
    }
})
