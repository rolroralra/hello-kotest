package com.example.hellokotest.coroutine

import java.math.BigDecimal

/**
 * 외부 환율 API를 호출한다고 가정하는 suspend 인터페이스.
 */
interface ExchangeRateClient {
    suspend fun getRate(from: String, to: String): BigDecimal
}
