package com.example.hellokotest.coroutines

import com.example.hellokotest.basic.Money
import com.example.hellokotest.coroutine.ExchangeRateClient
import com.example.hellokotest.coroutine.PriceConverter
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * 11-coroutines.md
 * Kotest 의 테스트 본문은 이미 suspend 함수이므로 runBlocking 없이 바로 suspend 함수를 호출할 수 있다.
 */
class CoroutineTest : FunSpec({

    test("suspend 함수를 그대로 호출할 수 있다") {
        val client = mockk<ExchangeRateClient>()
        coEvery { client.getRate("USD", "KRW") } returns BigDecimal("1400")

        val converter = PriceConverter(client)

        converter.convert(Money.of(10, "USD"), "KRW") shouldBe Money.of(14_000, "KRW")
        coVerify(exactly = 1) { client.getRate("USD", "KRW") }
    }

    test("같은 통화는 외부 호출 없이 그대로 돌려준다") {
        val client = mockk<ExchangeRateClient>()

        PriceConverter(client).convert(Money.of(10, "KRW"), "krw") shouldBe Money.of(10, "KRW")
        coVerify(exactly = 0) { client.getRate(any(), any()) }
    }

    test("가상 시간(coroutineTestScope) - 1시간 delay 도 즉시 끝난다").config(coroutineTestScope = true) {
        val slowClient = object : ExchangeRateClient {
            override suspend fun getRate(from: String, to: String): BigDecimal {
                delay(1.hours)
                return BigDecimal.ONE
            }
        }
        val converter = PriceConverter(slowClient, timeout = 3.seconds)

        shouldThrow<TimeoutCancellationException> {
            converter.convert(Money.of(1, "USD"), "KRW")
        }
        // 실제로는 거의 0ms 가 걸리지만 가상 시계는 3초가 흘렀다.
        testCoroutineScheduler.currentTime shouldBe 3.seconds.inWholeMilliseconds
    }

    test("병렬 호출 - convertAll 은 각 요청을 동시에 실행한다").config(coroutineTestScope = true) {
        val client = object : ExchangeRateClient {
            override suspend fun getRate(from: String, to: String): BigDecimal {
                delay(1.seconds)
                return BigDecimal("2")
            }
        }

        val result = PriceConverter(client).convertAll(
            listOf(Money.of(1, "USD"), Money.of(2, "USD"), Money.of(3, "USD")),
            "EUR",
        )

        result shouldContainExactly listOf(Money.of(2, "EUR"), Money.of(4, "EUR"), Money.of(6, "EUR"))
        // 순차 실행이라면 3초, 병렬이므로 1초
        testCoroutineScheduler.currentTime shouldBe 1.seconds.inWholeMilliseconds
    }

    test("eventually - 비동기 작업이 끝날 때까지 조건을 반복 확인한다") {
        val counter = AtomicInteger(0)
        launch {
            repeat(5) {
                delay(50.milliseconds)
                counter.incrementAndGet()
            }
        }

        eventually(2.seconds) {
            counter.get() shouldBe 5
        }
    }
})
