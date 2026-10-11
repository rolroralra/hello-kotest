# 05. 검증(Assertions)과 매처

> 실습 코드: [`assertions/BasicMatchersTest.kt`](../src/test/kotlin/com/example/hellokotest/assertions/BasicMatchersTest.kt), [`assertions/CollectionMatchersTest.kt`](../src/test/kotlin/com/example/hellokotest/assertions/CollectionMatchersTest.kt)

Kotest Assertions 는 `shouldXxx` 형태의 **매처(Matcher)** 를 300개 이상 제공합니다. 모두 외울 필요는 없고, IDE 에서 `actual.should` 까지 입력한 뒤 자동완성으로 찾는 것이 가장 빠릅니다.

## 두 가지 사용 방식

```kotlin
// 1. 확장 함수 방식 (추천)
greeting shouldStartWith "Hello"

// 2. should + 매처 함수 방식
greeting should startWith("Hello")
greeting shouldNot beBlank()
```

대부분 확장 함수 방식이 더 읽기 쉽습니다. 2번 방식은 매처를 조합하거나 [커스텀 매처](06-exceptions-and-soft-assertions.md#커스텀-매처)를 만들 때 사용합니다.

## 동등성

```kotlin
(1 + 1) shouldBe 2
"kotlin" shouldNotBe "java"

// data class 는 equals 로 비교
Member(1, "홍길동", "hong@example.com") shouldBe Member(1, "홍길동", "hong@example.com")
```

`shouldBe` 는 `equals()` 로 비교합니다. data class 비교가 실패하면 **어떤 필드가 다른지** 알려줍니다.

## 문자열

| 매처 | 의미 |
|------|------|
| `shouldStartWith` / `shouldEndWith` | 접두사 / 접미사 |
| `shouldContain` | 부분 문자열 포함 |
| `shouldHaveLength` | 길이 |
| `shouldBeBlank()` / `shouldBeEmpty()` | 공백 / 빈 문자열 |
| `shouldMatch` | 정규식 전체 일치 |
| `shouldBeEqualIgnoringCase` | 대소문자 무시 비교 |

```kotlin
val greeting = "Hello, Kotest!"

greeting shouldStartWith "Hello"
greeting shouldEndWith "!"
greeting shouldContain "Kotest"
greeting shouldHaveLength 14
"010-1234-5678" shouldMatch Regex("""\d{3}-\d{4}-\d{4}""")
```

## 숫자

```kotlin
10.shouldBePositive()
10.shouldBeEven()
10.shouldBeBetween(1, 100)
10 shouldBeGreaterThan 5
```

실수(Double) 는 오차 범위를 지정해 비교합니다: `0.1 + 0.2 shouldBe (0.3 plusOrMinus 0.0001)`

## nullable 과 타입

```kotlin
val nothing: String? = null
nothing.shouldBeNull()

val name: String? = "kotest"
name.shouldNotBeNull()
name.length shouldBe 6          // 스마트 캐스트: 이제 name 은 String 타입

val value: Any = "I am a String"
val str = value.shouldBeInstanceOf<String>()   // 캐스트된 값을 돌려준다
```

Kotest 의 `shouldNotBeNull()` 은 Kotlin **contract** 를 사용하므로, 검증 이후 코드에서 `!!` 없이 non-null 타입으로 쓸 수 있습니다. JUnit 의 `assertNotNull` 로는 불가능한 Kotlin 다운 장점입니다.

## 컬렉션

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

numbers shouldHaveSize 5
numbers shouldContain 3
numbers shouldNotContain 10
numbers.shouldContainAll(1, 5)                 // 포함 여부만, 순서 무관
numbers.shouldContainExactly(1, 2, 3, 4, 5)    // 원소와 순서까지 정확히
listOf("b", "a", "c").shouldContainExactlyInAnyOrder("a", "b", "c")
numbers.shouldBeSorted()
emptyList<String>().shouldBeEmpty()
```

| 매처 | 원소 일치 | 순서 일치 | 다른 원소 허용 |
|------|-----------|-----------|----------------|
| `shouldContainAll` | O | X | O |
| `shouldContainExactlyInAnyOrder` | O | X | X |
| `shouldContainExactly` | O | O | X |

## 맵

```kotlin
val scores = mapOf("kim" to 90, "lee" to 80)

scores shouldContainKey "kim"
scores shouldNotContainKey "park"
scores.shouldContain("lee", 80)
```

## Inspector - 모든 원소 검사

컬렉션의 각 원소에 대해 검증을 반복할 때 사용합니다. 실패하면 **어떤 원소가 실패했는지** 모두 알려줍니다.

```kotlin
numbers.forAll { it shouldBeGreaterThan 0 }         // 모든 원소가 통과
numbers.forAtLeastOne { it shouldBeGreaterThan 4 }  // 하나 이상 통과
numbers.forNone { it shouldBeGreaterThan 5 }        // 아무것도 통과하지 않음
```

`forExactly(n)`, `forAtMost(n)`, `forOne` 도 있습니다.

## import 팁

매처는 대상 타입별로 패키지가 나뉘어 있습니다. IDE 자동 import 가 엉뚱한 패키지를 고르면 아래 표를 참고하세요.

| 패키지 | 내용 |
|--------|------|
| `io.kotest.matchers` | `shouldBe`, `should`, `shouldNot` |
| `io.kotest.matchers.string` | 문자열 |
| `io.kotest.matchers.ints`, `longs`, `doubles` | 숫자 |
| `io.kotest.matchers.collections` | List, Set 등 |
| `io.kotest.matchers.maps` | Map |
| `io.kotest.matchers.nulls` | null |
| `io.kotest.matchers.types` | 타입 |
| `io.kotest.inspectors` | `forAll`, `forNone` 등 |

## 연습 문제

1. `listOf("apple", "banana")` 가 `"apple"` 을 포함하고 크기가 2 인지 검증하세요.
2. `"kotest@example.com"` 이 이메일 형식 정규식과 일치하는지 검증하세요.

## 정리

- `actual shouldXxx expected` 형태로 읽기 쉬운 검증을 작성합니다.
- `shouldNotBeNull()`, `shouldBeInstanceOf<T>()` 는 스마트 캐스트를 지원합니다.
- 컬렉션은 `shouldContainExactly` 와 `shouldContainExactlyInAnyOrder` 의 차이를 기억하세요.

---

[← 이전: 04. 테스트 스타일(Spec)](04-spec-styles.md) | [목차](../README.md) | [다음: 06. 예외 검증과 Soft Assertion →](06-exceptions-and-soft-assertions.md)
