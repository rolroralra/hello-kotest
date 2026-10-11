package com.example.hellokotest.advanced

import com.example.hellokotest.basic.Calculator
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * 13-advanced-config.md - 태그, 테스트 설정(config), 비활성화
 */
object Slow : Tag()
object Fast : Tag()

class TagsAndConfigTest : FunSpec({

    // Spec 의 모든 테스트에 태그를 붙인다.
    tags(Fast)

    val calculator = Calculator()

    test("태그를 붙인 테스트").config(tags = setOf(Slow)) {
        delay(100.milliseconds)
        calculator.add(1, 1) shouldBe 2
    }

    test("timeout - 지정 시간 안에 끝나야 한다").config(timeout = 1.seconds) {
        delay(10.milliseconds)
    }

    val invocationCount = AtomicInteger(0)
    test("invocations - 같은 테스트를 여러 번 실행한다").config(invocations = 3) {
        invocationCount.incrementAndGet()
    }

    test("enabled = false 로 비활성화").config(enabled = false) {
        error("실행되지 않는다")
    }

    test("enabledIf - 조건부 실행").config(enabledIf = { System.getenv("CI") == null }) {
        calculator.multiply(2, 2) shouldBe 4
    }

    xtest("xtest 로 비활성화") {
        error("실행되지 않는다")
    }

    test("!이름 앞에 느낌표를 붙여도 비활성화된다") {
        error("실행되지 않는다")
    }

    afterSpec {
        invocationCount.get() shouldBe 3
    }
})
