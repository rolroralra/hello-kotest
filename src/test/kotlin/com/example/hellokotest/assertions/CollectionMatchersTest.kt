package com.example.hellokotest.assertions

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.inspectors.forAtLeastOne
import io.kotest.inspectors.forNone
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSorted
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.string.shouldStartWith

/**
 * 05-assertions.md - 컬렉션 / 맵 / Inspector
 */
class CollectionMatchersTest : FunSpec({

    val numbers = listOf(1, 2, 3, 4, 5)

    test("리스트 매처") {
        numbers shouldHaveSize 5
        numbers shouldContain 3
        numbers shouldNotContain 10
        numbers.shouldContainAll(1, 5)
        numbers.shouldContainExactly(1, 2, 3, 4, 5)
        numbers.shouldBeSorted()
        emptyList<String>().shouldBeEmpty()
    }

    test("순서 상관없이 비교") {
        listOf("b", "a", "c").shouldContainExactlyInAnyOrder("a", "b", "c")
    }

    test("맵 매처") {
        val scores = mapOf("kim" to 90, "lee" to 80)

        scores shouldContainKey "kim"
        scores shouldNotContainKey "park"
        scores.shouldContain("lee", 80)
    }

    test("Inspector - 모든 원소에 대해 검증") {
        numbers.forAll { it shouldBeGreaterThan 0 }
        numbers.forAtLeastOne { it shouldBeGreaterThan 4 }
        listOf("apple", "avocado").forAll { it shouldStartWith "a" }
        numbers.forNone { it shouldBeGreaterThan 5 }
    }
})
