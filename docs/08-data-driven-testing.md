# 08. 데이터 기반 테스트

> 실습 코드: [`datadriven/DataDrivenTest.kt`](../src/test/kotlin/com/example/hellokotest/datadriven/DataDrivenTest.kt)

같은 로직을 여러 입력값으로 검증하고 싶을 때 사용합니다. JUnit 의 `@ParameterizedTest` 에 해당하며, Kotest 에서는 `withData` 를 씁니다. **입력값마다 별도의 테스트가 생성**되므로, 어떤 입력에서 실패했는지 바로 알 수 있습니다.

> Kotest 6 에서는 데이터 테스트 기능이 엔진에 포함되어 있어 별도 의존성이 필요 없습니다. (`import io.kotest.datatest.withData`)

## data class 로 입력과 기대값 묶기

```kotlin
data class AddCase(val a: Int, val b: Int, val expected: Int)

class DataDrivenTest : FunSpec({
    val calculator = Calculator()

    context("덧셈 - data class 를 데이터로 사용") {
        withData(
            AddCase(1, 1, 2),
            AddCase(-1, 1, 0),
            AddCase(100, 200, 300),
        ) { (a, b, expected) ->            // 구조 분해
            calculator.add(a, b) shouldBe expected
        }
    }
})
```

실행 결과 (data class 의 `toString()` 이 테스트 이름이 됩니다):

```
DataDrivenTest > 덧셈 - data class 를 데이터로 사용 > AddCase(a=1, b=1, expected=2) PASSED
DataDrivenTest > 덧셈 - data class 를 데이터로 사용 > AddCase(a=-1, b=1, expected=0) PASSED
DataDrivenTest > 덧셈 - data class 를 데이터로 사용 > AddCase(a=100, b=200, expected=300) PASSED
```

## 단순 값 목록

```kotlin
withData("Kotest2026", "Password1", "ABCdef1234") { password ->
    PasswordValidator.validate(password).shouldBeEmpty()
}
```

## 테스트 이름 지정하기

### 1. WithDataTestName 인터페이스

```kotlin
data class PasswordCase(val password: String, val violation: Violation) : WithDataTestName {
    override fun dataTestName() = "'$password' -> $violation"
}

withData(
    PasswordCase("Short1", Violation.TOO_SHORT),
    PasswordCase("NoDigitsHere", Violation.NO_DIGIT),
) { (password, violation) ->
    PasswordValidator.validate(password) shouldContain violation
}
```

```
'Short1' -> TOO_SHORT PASSED
'NoDigitsHere' -> NO_DIGIT PASSED
```

### 2. nameFn 파라미터

```kotlin
withData(
    nameFn = { (a, b, expected) -> "$a / $b = $expected" },
    Triple(10, 2, 5),
    Triple(7, 2, 3),
) { (a, b, expected) ->
    calculator.divide(a, b) shouldBe expected
}
```

### 3. Map 의 key 를 이름으로

```kotlin
withData(
    mapOf(
        "양수 곱셈" to Triple(2, 3, 6),
        "음수 곱셈" to Triple(-2, 3, -6),
    ),
) { (a, b, expected) ->
    calculator.multiply(a, b) shouldBe expected
}
```

## withData 를 쓸 수 있는 위치

- `FunSpec` 의 최상위 또는 `context` 안
- `DescribeSpec` 의 `describe` 안, `BehaviorSpec` 의 `Given` / `When` 안 등 **모든 스타일의 컨테이너**
- `withData` 안에 `withData` 를 중첩하면 조합(카테시안 곱) 테스트도 만들 수 있습니다.

### withData / withTests / withContexts

Kotest 6.2 에는 비슷한 함수가 세 개 있습니다.

| 함수 | 데이터마다 만들어지는 것 | 블록 안에서 `test(...)` 추가 |
|------|--------------------------|-------------------------------|
| `withData` | 컨테이너 (하위 테스트가 없으면 하나의 테스트로 보고됨) | 가능 |
| `withContexts` | 컨테이너 | 가능 |
| `withTests` | 말단 테스트 | 불가능 |

대부분은 `withData` 로 충분합니다. 데이터마다 여러 하위 테스트를 만들고 싶다면 블록 안에서 `test(...)` 를 선언하면 됩니다.

## JUnit 과 비교

```kotlin
// JUnit 5
@ParameterizedTest
@CsvSource("1, 1, 2", "-1, 1, 0", "100, 200, 300")
fun add(a: Int, b: Int, expected: Int) {
    assertEquals(expected, calculator.add(a, b))
}
```

Kotest 는 문자열(CSV)이 아니라 **타입이 있는 Kotlin 객체**를 그대로 쓰므로, 오타나 타입 변환 문제가 컴파일 시점에 잡힙니다.

## 연습 문제

1. `PasswordValidator` 에 `"Valid Pass1"` 을 넣었을 때 `CONTAINS_WHITESPACE` 가 나오는 케이스를 추가하세요.
2. `Calculator.subtract` 에 대한 데이터 기반 테스트를 `nameFn` 으로 작성하세요.

## 정리

- `withData(값...) { 값 -> 검증 }` 으로 파라미터화 테스트를 작성합니다.
- 테스트 이름은 `toString()`, `WithDataTestName`, `nameFn`, `Map` 의 key 로 지정합니다.
- 입력값마다 독립된 테스트가 만들어져 실패 지점을 바로 알 수 있습니다.

---

[← 이전: 07. 라이프사이클과 격리 모드](07-lifecycle-and-isolation.md) | [목차](../README.md) | [다음: 09. 속성 기반 테스트 →](09-property-based-testing.md)
