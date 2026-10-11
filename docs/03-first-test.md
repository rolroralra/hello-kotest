# 03. 첫 번째 테스트

> 실습 코드: [`basics/FirstTest.kt`](../src/test/kotlin/com/example/hellokotest/basics/FirstTest.kt)

## 테스트 대상

간단한 계산기 클래스를 테스트합니다.

```kotlin
// src/main/kotlin/com/example/hellokotest/basic/Calculator.kt
class Calculator {
    fun add(a: Int, b: Int): Int = a + b
    fun subtract(a: Int, b: Int): Int = a - b
    fun multiply(a: Int, b: Int): Int = a * b
    fun divide(a: Int, b: Int): Int {
        if (b == 0) throw ArithmeticException("0으로 나눌 수 없습니다.")
        return a / b
    }
}
```

## 테스트 작성

```kotlin
package com.example.hellokotest.basics

import com.example.hellokotest.basic.Calculator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FirstTest : StringSpec({

    val calculator = Calculator()

    "1 더하기 2 는 3 이다" {
        calculator.add(1, 2) shouldBe 3
    }

    "5 빼기 3 은 2 이다" {
        calculator.subtract(5, 3) shouldBe 2
    }

    "곱셈과 나눗셈" {
        calculator.multiply(4, 5) shouldBe 20
        calculator.divide(10, 2) shouldBe 5
    }
})
```

### 코드 뜯어보기

1. **`class FirstTest : StringSpec({ ... })`**
   테스트 클래스는 **Spec** 을 상속합니다. `StringSpec` 은 가장 단순한 스타일로, 생성자에 람다를 넘겨 그 안에 테스트를 정의합니다.

2. **`"1 더하기 2 는 3 이다" { ... }`**
   문자열 뒤에 중괄호 블록을 붙이면 그 자체가 하나의 테스트가 됩니다. 문자열은 테스트 이름이므로 한글로 자유롭게 써도 됩니다.

3. **`calculator.add(1, 2) shouldBe 3`**
   `shouldBe` 는 중위(infix) 함수입니다. `실제값 shouldBe 기대값` 순서로 읽습니다.

4. **`val calculator = Calculator()`**
   람다 안, 테스트 바깥에 선언한 변수는 모든 테스트가 공유합니다. (공유 방식은 [07. 라이프사이클과 격리 모드](07-lifecycle-and-isolation.md)에서 자세히 다룹니다.)

## 실행하기

```bash
./gradlew test --tests 'com.example.hellokotest.basics.FirstTest'
```

```
FirstTest > 1 더하기 2 는 3 이다 PASSED
FirstTest > 5 빼기 3 은 2 이다 PASSED
FirstTest > 곱셈과 나눗셈 PASSED
```

## 일부러 실패시켜 보기

기대값을 `4` 로 바꾸고 실행해 보세요.

```kotlin
"1 더하기 2 는 3 이다" {
    calculator.add(1, 2) shouldBe 4
}
```

```
FirstTest > 1 더하기 2 는 3 이다 FAILED
    org.opentest4j.AssertionFailedError: expected:<4> but was:<3>
```

`expected`(기대값) 와 `but was`(실제값) 가 명확하게 표시됩니다. 실패 메시지를 읽는 습관을 들이면 디버깅 시간이 크게 줄어듭니다.

> 실습 후에는 다시 `3` 으로 되돌려 주세요.

## 한 테스트에 여러 검증

`"곱셈과 나눗셈"` 테스트처럼 하나의 테스트에 여러 `shouldBe` 를 쓸 수 있습니다. 단, 첫 번째 검증이 실패하면 그 아래는 실행되지 않습니다. 모든 검증 결과를 한 번에 보고 싶다면 [06. 예외 검증과 Soft Assertion](06-exceptions-and-soft-assertions.md) 의 `assertSoftly` 를 사용하세요.

## 연습 문제

1. `FirstTest` 에 "10 나누기 3 은 3 이다" 테스트를 추가해 보세요.
2. `"".isEmpty() shouldBe true` 를 검증하는 테스트를 추가해 보세요.

## 정리

- Spec 을 상속하고, 그 안에 문자열 이름 + 람다로 테스트를 정의합니다.
- `실제값 shouldBe 기대값` 으로 검증합니다.
- 실패 메시지의 `expected` / `but was` 를 확인하세요.

---

[← 이전: 02. 프로젝트 설정](02-project-setup.md) | [목차](../README.md) | [다음: 04. 테스트 스타일(Spec) →](04-spec-styles.md)
