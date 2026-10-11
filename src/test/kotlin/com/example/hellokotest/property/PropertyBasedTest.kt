package com.example.hellokotest.property

import com.example.hellokotest.basic.Calculator
import com.example.hellokotest.basic.Money
import com.example.hellokotest.basic.PasswordValidator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll

/**
 * 09-property-based-testing.md
 * 예시 몇 개가 아니라 "항상 성립해야 하는 성질" 을 무작위 입력 수백 개로 검증한다.
 */

// 커스텀 Arb: 항상 규칙을 만족하는 비밀번호를 만든다.
val validPasswordArb: Arb<String> = arbitrary {
    val upper = Arb.char('A'..'Z').bind()
    val digit = Arb.char('0'..'9').bind()
    val rest = Arb.string(minSize = 6, maxSize = 18, codepoints = Codepoint.alphanumeric()).bind()
    (upper + rest + digit)
}

// Arb.bind: 여러 Arb 를 조합해 객체를 만든다.
val krwMoneyArb: Arb<Money> = Arb.bind(Arb.long(0L..1_000_000L), Arb.element("KRW")) { amount, currency ->
    Money.of(amount, currency)
}

class PropertyBasedTest : FunSpec({

    val calculator = Calculator()
    val smallInt = Arb.int(-10_000..10_000)

    test("덧셈의 교환법칙: a + b == b + a") {
        checkAll(smallInt, smallInt) { a, b ->
            calculator.add(a, b) shouldBe calculator.add(b, a)
        }
    }

    test("forAll 은 Boolean 을 반환하는 람다를 받는다") {
        forAll(smallInt) { a ->
            calculator.subtract(calculator.add(a, 10), 10) == a
        }
    }

    test("반복 횟수 지정 - 0 이 아닌 수로 나누고 곱하면 나머지를 제외하고 원래 값") {
        val nonZero = Arb.int(1..1_000)
        checkAll(1_000, smallInt, nonZero) { a, b ->
            calculator.multiply(calculator.divide(a, b), b) + (a % b) shouldBe a
        }
    }

    test("커스텀 Arb - 생성한 비밀번호는 항상 유효하다") {
        checkAll(validPasswordArb) { password ->
            PasswordValidator.isValid(password) shouldBe true
        }
    }

    test("Money 덧셈의 결합법칙") {
        checkAll(krwMoneyArb, krwMoneyArb, krwMoneyArb) { a, b, c ->
            (a + b) + c shouldBe a + (b + c)
        }
    }

    test("seed 를 고정하면 매번 같은 입력으로 재현할 수 있다") {
        checkAll(PropTestConfig(seed = 1234L), krwMoneyArb) { money ->
            (money - money).isZero() shouldBe true
        }
    }
})
