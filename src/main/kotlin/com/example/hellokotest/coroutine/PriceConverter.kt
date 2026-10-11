package com.example.hellokotest.coroutine

import com.example.hellokotest.basic.Money
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 코루틴 테스트(11) 실습 대상.
 */
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
