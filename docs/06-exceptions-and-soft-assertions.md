# 06. 예외 검증과 Soft Assertion

> 실습 코드: [`exceptions/ExceptionTest.kt`](../src/test/kotlin/com/example/hellokotest/exceptions/ExceptionTest.kt), [`exceptions/SoftAssertionAndClueTest.kt`](../src/test/kotlin/com/example/hellokotest/exceptions/SoftAssertionAndClueTest.kt)

## 예외 검증

### shouldThrow

```kotlin
val exception = shouldThrow<ArithmeticException> {
    calculator.divide(1, 0)
}
exception.message shouldBe "0으로 나눌 수 없습니다."
```

- 블록 안에서 지정한 타입의 예외가 발생하지 않으면 테스트가 실패합니다.
- **발생한 예외 객체를 돌려주므로** 메시지나 필드를 이어서 검증할 수 있습니다.

### 메시지까지 한 번에

```kotlin
shouldThrowWithMessage<ArithmeticException>("0으로 나눌 수 없습니다.") {
    calculator.divide(1, 0)
}
```

### shouldThrow vs shouldThrowExactly

```kotlin
// IllegalArgumentException 은 RuntimeException 의 하위 타입
shouldThrow<RuntimeException> { Money.of(1000, "WONS") }                  // 통과 (하위 타입 허용)
shouldThrowExactly<IllegalArgumentException> { Money.of(1000, "WONS") }   // 통과 (정확한 타입)
```

| 함수 | 하위 타입 허용 |
|------|----------------|
| `shouldThrow<T>` | O |
| `shouldThrowExactly<T>` | X |
| `shouldThrowAny` | 모든 예외 |

### 예외가 없어야 할 때

```kotlin
shouldNotThrowAny {
    calculator.divide(10, 3)
}
```

## Soft Assertion - assertSoftly

일반적으로는 첫 번째 검증이 실패하면 테스트가 바로 중단됩니다. 객체의 여러 필드를 검증할 때 **실패한 항목을 한 번에 모두** 보고 싶다면 `assertSoftly` 를 씁니다.

```kotlin
val member = Member(1, "홍길동", "hong@example.com")

assertSoftly(member) {        // 람다 안에서 this == member
    id shouldBe 1
    name shouldBe "홍길동"
    email shouldEndWith "@example.com"
}
```

두 개가 실패하면 다음처럼 모아서 보여줍니다.

```
The following 2 assertions failed:
1) expected:<임꺽정> but was:<홍길동>
   at ...SoftAssertionAndClueTest.kt:50
2) expected:<lim@example.com> but was:<hong@example.com>
   at ...SoftAssertionAndClueTest.kt:51
```

## withClue - 실패 메시지에 맥락 추가

반복문이나 여러 객체를 검증할 때, 실패 메시지만으로는 **어떤 데이터에서** 실패했는지 알기 어렵습니다. `withClue` 로 설명을 덧붙일 수 있습니다.

```kotlin
withClue("id=${member.id} 회원의 이메일") {
    member.email shouldBe "other@example.com"
}
```

```
id=1 회원의 이메일
expected:<other@example.com> but was:<hong@example.com>
```

`asClue` 를 사용하면 객체 자체를 단서로 쓸 수 있습니다: `member.asClue { it.name shouldBe "홍길동" }`

## 커스텀 매처

도메인에 특화된 검증을 반복해서 쓴다면 매처를 직접 만들 수 있습니다.

```kotlin
// 1. Matcher 를 반환하는 함수
fun haveCurrency(currency: String) = Matcher<Money> { value ->
    MatcherResult(
        value.currency == currency,                                        // 통과 조건
        { "통화가 $currency 이어야 하지만 ${value.currency} 입니다." },        // should 실패 메시지
        { "통화가 $currency 가 아니어야 합니다." },                           // shouldNot 실패 메시지
    )
}

// 2. (선택) 읽기 좋은 확장 함수
infix fun Money.shouldHaveCurrency(currency: String): Money {
    this should haveCurrency(currency)
    return this
}
```

```kotlin
val price = Money.of(1000, "krw")

price should haveCurrency("KRW")
price shouldNot haveCurrency("USD")    // 하나의 매처로 부정형도 자동 지원
price shouldHaveCurrency "KRW"
```

## 연습 문제

1. `PasswordValidator.validate("abc")` 결과에 `TOO_SHORT`, `NO_DIGIT`, `NO_UPPERCASE` 가 모두 있는지 `assertSoftly` 로 검증하세요.
2. `Money` 가 0원인지 검사하는 `beZero()` 커스텀 매처를 만들어 보세요.

## 정리

- `shouldThrow<T>` 는 예외를 돌려주므로 후속 검증이 가능합니다.
- `assertSoftly` 는 실패를 모아 한 번에 보고합니다.
- `withClue` 로 실패 메시지에 맥락을 더하세요.
- 반복되는 도메인 검증은 커스텀 매처로 만듭니다.

---

[← 이전: 05. 검증(Assertions)과 매처](05-assertions.md) | [목차](../README.md) | [다음: 07. 라이프사이클과 격리 모드 →](07-lifecycle-and-isolation.md)
