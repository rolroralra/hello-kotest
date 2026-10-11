# 09. 속성 기반 테스트

> 실습 코드: [`property/PropertyBasedTest.kt`](../src/test/kotlin/com/example/hellokotest/property/PropertyBasedTest.kt)

## 예시 기반 vs 속성 기반

지금까지의 테스트는 **예시 기반(example-based)** 이었습니다. 개발자가 직접 고른 입력 몇 개를 검증하죠. 하지만 개발자는 자신이 생각하지 못한 입력(음수, 0, 최댓값, 빈 문자열, 특수 문자...)을 놓치기 쉽습니다.

**속성 기반 테스트(property-based testing)** 는 "어떤 입력이 와도 항상 성립해야 하는 성질(property)" 을 정의하고, 프레임워크가 **무작위 입력을 수백~수천 개 생성**해 검증합니다.

| 예시 기반 | 속성 기반 |
|-----------|-----------|
| `add(1, 2) == 3` | 모든 a, b 에 대해 `add(a, b) == add(b, a)` |
| 입력을 개발자가 고름 | 입력을 프레임워크가 생성 |
| 놓친 경계값은 검증 못 함 | 경계값(0, 최솟값, 최댓값 등)을 자동으로 섞어 넣음 |

## checkAll - 검증(assertion) 기반

```kotlin
val smallInt = Arb.int(-10_000..10_000)

test("덧셈의 교환법칙: a + b == b + a") {
    checkAll(smallInt, smallInt) { a, b ->
        calculator.add(a, b) shouldBe calculator.add(b, a)
    }
}
```

- `Arb` (Arbitrary) 는 무작위 값을 만드는 **생성기**입니다.
- 기본적으로 **1,000번** 실행됩니다. 람다 안의 검증이 한 번이라도 실패하면 테스트가 실패합니다.

## forAll - Boolean 기반

```kotlin
forAll(smallInt) { a ->
    calculator.subtract(calculator.add(a, 10), 10) == a
}
```

람다가 `true` / `false` 를 반환합니다. 단순한 성질은 `forAll`, 여러 검증이 필요하면 `checkAll` 을 쓰세요.

## 반복 횟수 지정

```kotlin
checkAll(1_000, smallInt, Arb.int(1..1_000)) { a, b ->
    calculator.multiply(calculator.divide(a, b), b) + (a % b) shouldBe a
}
```

## 자주 쓰는 Arb

| 생성기 | 설명 |
|--------|------|
| `Arb.int()`, `Arb.int(1..100)` | 정수 |
| `Arb.long()`, `Arb.double()` | 숫자 |
| `Arb.string(minSize, maxSize, codepoints)` | 문자열 |
| `Arb.char('A'..'Z')` | 문자 |
| `Arb.element("KRW", "USD")` | 주어진 값 중 하나 |
| `Arb.list(Arb.int(), 0..10)` | 리스트 |
| `Arb.email()`, `Arb.uuid()`, `Arb.localDate()` | 자주 쓰는 도메인 값 |
| `Arb.enum<Violation>()` | enum |

## 커스텀 Arb 만들기

### arbitrary { } 빌더

항상 규칙을 만족하는 비밀번호를 만드는 생성기입니다.

```kotlin
val validPasswordArb: Arb<String> = arbitrary {
    val upper = Arb.char('A'..'Z').bind()
    val digit = Arb.char('0'..'9').bind()
    val rest = Arb.string(minSize = 6, maxSize = 18, codepoints = Codepoint.alphanumeric()).bind()
    upper + rest + digit
}

test("생성한 비밀번호는 항상 유효하다") {
    checkAll(validPasswordArb) { password ->
        PasswordValidator.isValid(password) shouldBe true
    }
}
```

`.bind()` 로 다른 Arb 에서 값을 하나 꺼내 쓸 수 있습니다.

### Arb.bind - 객체 조합

```kotlin
val krwMoneyArb: Arb<Money> = Arb.bind(Arb.long(0L..1_000_000L), Arb.element("KRW")) { amount, currency ->
    Money.of(amount, currency)
}

test("Money 덧셈의 결합법칙") {
    checkAll(krwMoneyArb, krwMoneyArb, krwMoneyArb) { a, b, c ->
        (a + b) + c shouldBe a + (b + c)
    }
}
```

## 실패하면? - Shrinking 과 Seed

속성 기반 테스트가 실패하면 Kotest 는 두 가지를 알려줍니다.

1. **Shrinking(축소)**: 실패한 입력을 더 단순한 값으로 줄여가며, 여전히 실패하는 **가장 작은 반례**를 찾아줍니다. 예를 들어 `a=-58213` 에서 실패했다면 `a=-1` 처럼 이해하기 쉬운 값으로 줄여서 보여줍니다.
2. **Seed**: 무작위 생성에 사용한 시드 값을 출력합니다. 같은 시드로 다시 실행하면 같은 입력이 재현됩니다.

```kotlin
checkAll(PropTestConfig(seed = 1234L), krwMoneyArb) { money ->
    (money - money).isZero() shouldBe true
}
```

> 실패를 재현한 뒤 버그를 고쳤다면, 해당 반례를 [데이터 기반 테스트](08-data-driven-testing.md)로 옮겨 회귀 테스트로 남겨 두는 것이 좋습니다.

## 어떤 성질을 테스트할까?

| 성질 | 예 |
|------|-----|
| 교환법칙 / 결합법칙 | `a + b == b + a` |
| 역연산 (round-trip) | `decode(encode(x)) == x`, JSON 직렬화 → 역직렬화 |
| 불변식(invariant) | 정렬 후 크기는 그대로, 잔액은 음수가 될 수 없음 |
| 멱등성 | `normalize(normalize(x)) == normalize(x)` |
| 다른 구현과 비교 (oracle) | 새 알고리즘 결과 == 기존(느리지만 정확한) 구현 결과 |

## 연습 문제

1. 모든 문자열 `s` 에 대해 `s.reversed().reversed() == s` 임을 검증하세요.
2. `Arb.list(Arb.int())` 로 만든 리스트를 정렬했을 때 크기가 변하지 않음을 검증하세요.

## 정리

- `Arb` 로 무작위 입력을 만들고 `checkAll` / `forAll` 로 성질을 검증합니다.
- 실패하면 Shrinking 으로 최소 반례를 찾고, Seed 로 재현할 수 있습니다.
- 예시 기반 테스트를 대체하는 것이 아니라 **보완**하는 도구입니다.

---

[← 이전: 08. 데이터 기반 테스트](08-data-driven-testing.md) | [목차](../README.md) | [다음: 10. MockK 로 목(Mock) 사용하기 →](10-mocking-with-mockk.md)
