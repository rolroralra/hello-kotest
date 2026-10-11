# 04. 테스트 스타일(Spec)

> 실습 코드: [`styles/`](../src/test/kotlin/com/example/hellokotest/styles)

Kotest 는 **10가지 테스트 스타일(Spec)** 을 제공합니다. 스타일마다 테스트를 정의하는 문법만 다를 뿐, 기능(매처, 설정, 라이프사이클)은 모두 같습니다. 팀이나 프로젝트 성격에 맞는 스타일 하나를 골라 일관되게 쓰는 것을 권장합니다.

## 한눈에 비교

| 스타일 | 형태 | 이런 경우에 추천 |
|--------|------|------------------|
| `StringSpec` | `"이름" { }` | 가장 간단한 테스트, 입문 |
| `FunSpec` | `test("이름") { }`, `context` | JUnit 에서 넘어온 경우, 범용 |
| `BehaviorSpec` | `Given` / `When` / `Then` | BDD, 비즈니스 시나리오 |
| `DescribeSpec` | `describe` / `it` | JS(Jest), Ruby(RSpec) 경험자 |
| `ShouldSpec` | `should("...")` | "~해야 한다" 문장형 |
| `FreeSpec` | `"이름" - { }` | 자유로운 깊이의 중첩 |
| `AnnotationSpec` | `@Test fun ...` | JUnit 테스트를 그대로 옮길 때 |
| `WordSpec`, `FeatureSpec`, `ExpectSpec` | - | 이 문서에서는 생략 |

## FunSpec

> [`FunSpecExample.kt`](../src/test/kotlin/com/example/hellokotest/styles/FunSpecExample.kt)

```kotlin
class FunSpecExample : FunSpec({
    val calculator = Calculator()

    test("덧셈") {
        calculator.add(2, 3) shouldBe 5
    }

    context("나눗셈") {
        test("나누어 떨어지는 경우") {
            calculator.divide(10, 5) shouldBe 2
        }
        test("나머지는 버린다") {
            calculator.divide(7, 2) shouldBe 3
        }
    }
})
```

- `test` 로 테스트를 정의합니다.
- `context` 로 관련 테스트를 그룹으로 묶습니다. `context` 안에 `context` 를 또 넣을 수 있습니다.
- 가장 범용적이라 **처음 시작할 때 추천하는 스타일**입니다.

## BehaviorSpec

> [`BehaviorSpecExample.kt`](../src/test/kotlin/com/example/hellokotest/styles/BehaviorSpecExample.kt)

```kotlin
class BehaviorSpecExample : BehaviorSpec({
    Given("지갑에 10,000원이 있을 때") {
        val wallet = Money.of(10_000, "KRW")

        When("3,000원을 사용하면") {
            val remain = wallet - Money.of(3_000, "KRW")

            Then("7,000원이 남는다") {
                remain shouldBe Money.of(7_000, "KRW")
            }
        }
    }
})
```

- **Given**(주어진 상황) → **When**(행동) → **Then**(결과) 구조로 시나리오를 표현합니다.
- 실제로 실행되는 테스트는 `Then` 블록입니다. `Given`, `When` 은 컨테이너입니다.
- 기획자와 함께 읽을 수 있을 만큼 비즈니스 규칙을 드러내기 좋습니다.
- 소문자 `given` / `when` 도 있지만 `when` 은 Kotlin 키워드라 `` `when` `` 처럼 백틱이 필요하므로 대문자를 주로 씁니다.

## DescribeSpec

> [`DescribeSpecExample.kt`](../src/test/kotlin/com/example/hellokotest/styles/DescribeSpecExample.kt)

```kotlin
class DescribeSpecExample : DescribeSpec({
    describe("PasswordValidator") {
        describe("isValid") {
            it("규칙을 모두 지키면 true") {
                PasswordValidator.isValid("Kotest2026") shouldBe true
            }
            it("숫자가 없으면 false") {
                PasswordValidator.isValid("KotestOnly") shouldBe false
            }
        }
    }
})
```

- `describe("대상")` → `it("동작")` 형태입니다. 클래스 → 메서드 → 케이스 순서로 중첩하면 구조가 깔끔합니다.

## ShouldSpec

> [`ShouldSpecExample.kt`](../src/test/kotlin/com/example/hellokotest/styles/ShouldSpecExample.kt)

```kotlin
class ShouldSpecExample : ShouldSpec({
    context("String.reversed()") {
        should("문자열을 뒤집는다") {
            "kotest".reversed() shouldBe "tsetok"
        }
    }
})
```

## FreeSpec

> [`FreeSpecExample.kt`](../src/test/kotlin/com/example/hellokotest/styles/FreeSpecExample.kt)

```kotlin
class FreeSpecExample : FreeSpec({
    "List" - {
        "원소가 3개인 리스트" - {
            val list = listOf(1, 2, 3)

            "합계는 6 이다" {
                list.sum() shouldBe 6
            }
        }
    }
})
```

- `"이름" - { }` 는 컨테이너(그룹), `"이름" { }` 는 테스트입니다.
- 마이너스(`-`) 를 빠뜨리면 컨테이너가 아니라 테스트가 되므로 주의하세요.

## AnnotationSpec

> [`AnnotationSpecExample.kt`](../src/test/kotlin/com/example/hellokotest/styles/AnnotationSpecExample.kt)

```kotlin
class AnnotationSpecExample : AnnotationSpec() {
    private lateinit var calculator: Calculator

    @BeforeEach
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun addition() {
        calculator.add(1, 1) shouldBe 2
    }
}
```

- JUnit 과 거의 같은 모양입니다. 단, 애너테이션은 `org.junit` 이 아니라 **`AnnotationSpec` 안에 정의된 것**(`AnnotationSpec.Test`)을 사용합니다.
- 기존 JUnit 테스트를 Kotest 로 옮기는 첫 단계로 쓰기 좋지만, Kotest 의 장점(중첩, DSL)은 살리기 어렵습니다.

## 생성자 람다 vs init 블록

지금까지는 `FunSpec({ ... })` 처럼 생성자에 람다를 넘겼습니다. 아래처럼 `init` 블록을 써도 완전히 같습니다.

```kotlin
class FunSpecExample : FunSpec() {
    init {
        test("덧셈") { ... }
    }
}
```

클래스의 프로퍼티(필드)가 필요할 때, 예를 들어 Spring 의 `@Autowired` 필드 주입을 쓸 때는 `init` 블록 방식을 사용합니다. ([12. Spring Boot 통합 테스트](12-spring-boot-integration.md) 참고)

## 어떤 스타일을 고를까?

- 처음이라면: **FunSpec** 또는 **StringSpec**
- 서비스 계층의 비즈니스 시나리오: **BehaviorSpec**
- 클래스 / 메서드 단위로 정리: **DescribeSpec**

이 프로젝트에서는 학습을 위해 여러 스타일을 섞어 썼지만, 실제 프로젝트에서는 1~2개로 통일하는 것이 좋습니다.

## 정리

- 스타일은 문법만 다르고 기능은 같습니다.
- 컨테이너(`context`, `describe`, `Given`/`When`)로 그룹을 만들고, 말단(`test`, `it`, `Then`)이 실제 테스트입니다.
- 팀에서 하나의 스타일로 통일하세요.

---

[← 이전: 03. 첫 번째 테스트](03-first-test.md) | [목차](../README.md) | [다음: 05. 검증(Assertions)과 매처 →](05-assertions.md)
