package com.example.hellokotest.basic

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 불변 값 객체. 연산 결과는 항상 새 인스턴스로 돌려준다.
 * 커스텀 매처(06)와 속성 기반 테스트(09)에서 사용한다.
 */
data class Money private constructor(val amount: BigDecimal, val currency: String) {

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return of(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return of(amount - other.amount, currency)
    }

    operator fun times(multiplier: Int): Money = of(amount * BigDecimal(multiplier), currency)

    fun isZero(): Boolean = amount.signum() == 0

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) { "통화가 다릅니다: $currency, ${other.currency}" }
    }

    companion object {
        private const val SCALE = 2

        fun of(amount: BigDecimal, currency: String): Money {
            require(currency.length == 3) { "통화 코드는 3자리여야 합니다: $currency" }
            return Money(amount.setScale(SCALE, RoundingMode.HALF_UP), currency.uppercase())
        }

        fun of(amount: Long, currency: String): Money = of(BigDecimal.valueOf(amount), currency)

        fun zero(currency: String): Money = of(BigDecimal.ZERO, currency)
    }
}
