package com.example.hellokotest.lifecycle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/**
 * 07-lifecycle-and-isolation.md - 라이프사이클 훅
 *
 * 실행 순서: beforeSpec -> (beforeEach -> test -> afterEach) x N -> afterSpec
 * 훅 호출 순서를 기록하기 위해 이 예제에서만 의도적으로 가변 리스트를 사용한다.
 */
class LifecycleHooksTest : FunSpec({

    val events = mutableListOf<String>()

    beforeSpec { events += "beforeSpec" }

    beforeEach { testCase -> events += "beforeEach(${testCase.name.name})" }

    afterEach { (testCase, result) ->
        events += "afterEach(${testCase.name.name}, passed=${result.isSuccess})"
    }

    test("첫 번째 테스트") {
        events shouldContainExactly listOf("beforeSpec", "beforeEach(첫 번째 테스트)")
    }

    test("두 번째 테스트") {
        events.last() shouldBe "beforeEach(두 번째 테스트)"
        events shouldContainExactly listOf(
            "beforeSpec",
            "beforeEach(첫 번째 테스트)",
            "afterEach(첫 번째 테스트, passed=true)",
            "beforeEach(두 번째 테스트)",
        )
    }

    afterSpec {
        // 모든 테스트가 끝난 뒤 한 번 실행된다. 보통 외부 리소스를 정리할 때 사용한다.
        println("afterSpec events=$events")
    }
})
