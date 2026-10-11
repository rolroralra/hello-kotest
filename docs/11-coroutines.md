# 11. 코루틴 테스트

> 실습 코드: [`coroutines/CoroutineTest.kt`](../src/test/kotlin/com/example/hellokotest/coroutines/CoroutineTest.kt)

## 테스트 본문은 이미 suspend

JUnit 에서 `suspend` 함수를 테스트하려면 `runTest { }` 나 `runBlocking { }` 으로 감싸야 합니다. **Kotest 의 테스트 블록은 그 자체가 `suspend` 람다**이므로 바로 호출할 수 있습니다.

```kotlin
// JUnit 5
@Test
fun convert() = runTest {
    converter.convert(...)
}

// Kotest
test("suspend 함수를 그대로 호출할 수 있다") {
    converter.convert(...)
}
```

## 테스트 대상

```kotlin
interface ExchangeRateClient {
    suspend fun getRate(from: String, to: String): BigDecimal
}

class PriceConverter(
    private val client: ExchangeRateClient,
    private val timeout: Duration = 3.seconds,
) {
    suspend fun convert(money: Money, to: String): Money {
        if (money.currency == to.uppercase()) return money
        val rate = withTimeout(timeout) { client.getRate(money.currency, to) }
        return Money.of(money.amount * rate, to)
    }

    /** 여러 금액을 동시에(병렬로) 변환한다. */
    suspend fun convertAll(moneys: List<Money>, to: String): List<Money> = coroutineScope {
        moneys.map { async { convert(it, to) } }.awaitAll()
    }
}
```

## suspend 함수 목: coEvery / coVerify

```kotlin
test("suspend 함수를 그대로 호출할 수 있다") {
    val client = mockk<ExchangeRateClient>()
    coEvery { client.getRate("USD", "KRW") } returns BigDecimal("1400")

    val converter = PriceConverter(client)

    converter.convert(Money.of(10, "USD"), "KRW") shouldBe Money.of(14_000, "KRW")
    coVerify(exactly = 1) { client.getRate("USD", "KRW") }
}
```

## 가상 시간 - coroutineTestScope

`delay(1.hours)` 가 들어간 코드를 테스트할 때 실제로 1시간을 기다릴 수는 없습니다. `coroutineTestScope = true` 를 설정하면 `kotlinx-coroutines-test` 의 **가상 시계**를 사용하므로 `delay` 가 즉시 끝납니다.

```kotlin
test("가상 시간 - 1시간 delay 도 즉시 끝난다").config(coroutineTestScope = true) {
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
```

- `testCoroutineScheduler` 로 가상 시계에 접근할 수 있습니다. (`import io.kotest.core.test.testCoroutineScheduler`)
- `testCoroutineScheduler.advanceTimeBy(...)`, `advanceUntilIdle()` 로 시간을 직접 진행시킬 수도 있습니다.
- Spec 전체에 적용하려면 Spec 본문에 `coroutineTestScope = true` 를 씁니다.

### 병렬 실행 검증

가상 시계를 이용하면 "정말 병렬로 실행되는가?" 도 검증할 수 있습니다.

```kotlin
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
```

## eventually - 비동기 결과 기다리기

메시지 큐 소비, 캐시 갱신처럼 **언젠가는** 완료되는 작업을 검증할 때 `Thread.sleep` 대신 `eventually` 를 사용합니다. 조건이 통과할 때까지 정해진 시간 동안 반복해서 확인합니다.

```kotlin
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
```

- 2초 안에 통과하면 바로 성공, 끝까지 통과하지 못하면 마지막 실패를 보고합니다.
- `Thread.sleep(2000)` 처럼 항상 최대 시간을 기다리지 않으므로 테스트가 빠르고 안정적입니다.
- 반대 개념으로 "일정 시간 동안 **계속** 성립해야 한다" 를 검증하는 `continually` 도 있습니다.

## 정리

- Kotest 테스트 블록은 `suspend` 이므로 `runBlocking` 이 필요 없습니다.
- `suspend` 함수 목은 `coEvery` / `coVerify`
- `coroutineTestScope = true` + `testCoroutineScheduler` 로 시간을 제어합니다.
- 비동기 결과는 `eventually` 로 기다립니다.

---

[← 이전: 10. MockK 로 목(Mock) 사용하기](10-mocking-with-mockk.md) | [목차](../README.md) | [다음: 12. Spring Boot 통합 테스트 →](12-spring-boot-integration.md)
