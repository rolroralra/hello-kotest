# 01. Kotest 소개

## Kotest 란?

[Kotest](https://kotest.io) 는 Kotlin 을 위한 테스트 프레임워크입니다. JUnit 이 Java 를 위해 만들어졌다면, Kotest 는 처음부터 Kotlin 의 문법(람다, 확장 함수, 코루틴, DSL)을 적극적으로 활용하도록 설계되었습니다.

Kotest 는 크게 세 부분으로 나뉘며, 각각 따로 사용할 수도 있습니다.

| 모듈 | 역할 | 이 프로젝트의 의존성 |
|------|------|------------------------|
| **Test Framework** | 테스트를 작성하고 실행하는 구조(Spec, 라이프사이클, 설정) | `kotest-runner-junit5` |
| **Assertions** | `shouldBe` 같은 검증 함수(매처) 300개 이상 | `kotest-assertions-core` |
| **Property Testing** | 무작위 입력으로 성질을 검증 | `kotest-property` |

> 예를 들어 JUnit 5 로 테스트를 작성하면서 검증 부분만 Kotest Assertions 를 써도 됩니다.

## JUnit 5 와 비교

같은 테스트를 JUnit 5 와 Kotest 로 작성해 보면 차이가 잘 보입니다.

```kotlin
// JUnit 5
class CalculatorJUnitTest {
    private val calculator = Calculator()

    @Test
    @DisplayName("1 더하기 2 는 3 이다")
    fun add() {
        assertEquals(3, calculator.add(1, 2))
    }
}
```

```kotlin
// Kotest
class CalculatorTest : StringSpec({
    val calculator = Calculator()

    "1 더하기 2 는 3 이다" {
        calculator.add(1, 2) shouldBe 3
    }
})
```

| 항목 | JUnit 5 | Kotest |
|------|---------|--------|
| 테스트 정의 | `@Test` 붙은 메서드 | 문자열 이름 + 람다 |
| 테스트 이름 | 메서드명 또는 `@DisplayName` | 문자열 자체가 이름 |
| 검증 | `assertEquals(expected, actual)` | `actual shouldBe expected` |
| 중첩 구조 | `@Nested` inner class | `context`, `describe`, `Given/When/Then` 등 |
| 파라미터 테스트 | `@ParameterizedTest` + Source 애너테이션 | `withData(...)` |
| 코루틴 | `runTest { }` 로 감싸야 함 | 테스트 본문이 이미 `suspend` |
| 속성 기반 테스트 | 별도 라이브러리 필요 | 기본 제공 (`checkAll`, `forAll`) |

## 왜 Kotest 를 쓸까?

1. **읽기 쉬운 테스트**: `actual shouldBe expected` 는 영어 문장처럼 읽힙니다. 기대값과 실제값의 순서를 헷갈릴 일도 없습니다.
2. **다양한 스타일**: 팀 취향에 맞게 BDD(Given/When/Then), Jest 스타일(describe/it) 등 10가지 스타일 중 선택할 수 있습니다.
3. **Kotlin 친화적**: 코루틴, nullable 타입의 스마트 캐스트, data class 비교 등을 자연스럽게 지원합니다.
4. **풍부한 기능**: 데이터 기반 테스트, 속성 기반 테스트, `eventually` 같은 비동기 검증, Spring 연동을 기본 제공합니다.

## JUnit Platform 위에서 동작

Kotest 는 JUnit Platform 의 **테스트 엔진**으로 동작합니다. 그래서 Gradle 의 `useJUnitPlatform()`, IntelliJ, CI 도구 등 기존 JUnit 생태계를 그대로 사용할 수 있습니다. 같은 프로젝트 안에서 JUnit 테스트와 Kotest 테스트가 함께 실행될 수도 있습니다.

## 이 프로젝트에서 사용하는 버전

| 항목 | 버전 |
|------|------|
| Kotlin | 2.3.21 (Spring Boot 4.1.1 관리 버전) |
| Spring Boot | 4.1.1 |
| Kotest | 6.2.5 |
| MockK | 1.14.11 |
| springmockk | 5.0.1 |
| JDK | 21 |
| Gradle | 9.7.1 (Kotlin DSL) |

## 정리

- Kotest = 테스트 프레임워크 + 검증(Assertions) + 속성 기반 테스트
- JUnit Platform 위에서 동작하므로 기존 도구와 호환됩니다.
- 문자열 이름과 람다로 테스트를 정의하고, `shouldBe` 로 검증합니다.

---

[← 이전: 목차(README)](../README.md) | [다음: 02. 프로젝트 설정 →](02-project-setup.md)
