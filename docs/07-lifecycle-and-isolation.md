# 07. 라이프사이클과 격리 모드

> 실습 코드: [`lifecycle/LifecycleHooksTest.kt`](../src/test/kotlin/com/example/hellokotest/lifecycle/LifecycleHooksTest.kt), [`lifecycle/IsolationModeTest.kt`](../src/test/kotlin/com/example/hellokotest/lifecycle/IsolationModeTest.kt)

## 라이프사이클 훅

테스트 전후에 준비/정리 코드를 실행하고 싶을 때 사용합니다. JUnit 의 `@BeforeEach`, `@AfterAll` 등에 해당합니다.

| Kotest | JUnit 5 | 실행 시점 |
|--------|---------|-----------|
| `beforeSpec { }` | `@BeforeAll` | Spec 시작 시 1번 |
| `afterSpec { }` | `@AfterAll` | Spec 종료 시 1번 |
| `beforeEach { }` | `@BeforeEach` | 각 **말단 테스트** 전 |
| `afterEach { }` | `@AfterEach` | 각 **말단 테스트** 후 |
| `beforeContainer { }` / `afterContainer { }` | - | 각 **컨테이너**(context, Given 등) 전후 |
| `beforeAny { }` / `afterAny { }` | - | 컨테이너 + 말단 모두 |

```kotlin
class LifecycleHooksTest : FunSpec({

    val events = mutableListOf<String>()

    beforeSpec { events += "beforeSpec" }

    beforeEach { testCase -> events += "beforeEach(${testCase.name.name})" }

    afterEach { (testCase, result) ->
        events += "afterEach(${testCase.name.name}, passed=${result.isSuccess})"
    }

    test("첫 번째 테스트") {
        events shouldContainExactly listOf("beforeSpec", "beforeEach(첫 번째 테스트)")
    }

    test("두 번째 테스트") {
        events shouldContainExactly listOf(
            "beforeSpec",
            "beforeEach(첫 번째 테스트)",
            "afterEach(첫 번째 테스트, passed=true)",
            "beforeEach(두 번째 테스트)",
        )
    }
})
```

- `beforeEach` 는 현재 `TestCase` 를, `afterEach` 는 `(TestCase, TestResult)` 를 받습니다. 테스트 이름이나 성공 여부를 확인할 수 있습니다.
- 훅은 여러 번 등록할 수 있고 **등록한 순서대로** 실행됩니다.

> `beforeTest` / `afterTest` 도 있습니다. 이 둘은 `beforeAny` / `afterAny` 와 같이 **컨테이너에서도** 실행되니, 말단 테스트에만 실행하려면 `beforeEach` / `afterEach` 를 쓰세요.

## 격리 모드(Isolation Mode)

JUnit 은 **테스트 메서드마다 테스트 클래스 인스턴스를 새로 만듭니다.** 반면 Kotest 의 기본값은 **Spec 인스턴스 하나를 모든 테스트가 공유**하는 것입니다. JUnit 에 익숙하다면 가장 헷갈리는 부분이니 꼭 이해하고 넘어가세요.

| 모드 | 동작 |
|------|------|
| `SingleInstance` (기본값) | Spec 인스턴스 1개를 모든 테스트가 공유 |
| `InstancePerRoot` | 최상위(root) 테스트마다 새 인스턴스 |
| `InstancePerLeaf` | 말단 테스트마다 새 인스턴스를 만들고, 그 테스트까지의 경로만 다시 실행 |

### SingleInstance - 상태가 공유된다

```kotlin
class SingleInstanceTest : FunSpec({
    val list = mutableListOf<String>()

    test("a") {
        list += "a"
        list shouldContainExactly listOf("a")
    }

    test("b - 이전 테스트에서 추가한 값이 남아 있다") {
        list += "b"
        list shouldContainExactly listOf("a", "b")
    }
})
```

Spec 본문(`val list`)은 단 한 번 실행되므로, 테스트 `a` 의 변경이 `b` 에 그대로 남습니다.

### InstancePerLeaf - 경로마다 깨끗하게

```kotlin
class InstancePerLeafTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    Given("빈 장바구니") {
        val cart = mutableListOf<String>()

        When("사과를 담으면") {
            cart += "apple"
            Then("장바구니에 사과 1개") {
                cart shouldContainExactly listOf("apple")
            }
        }

        When("바나나를 담으면") {
            cart += "banana"
            Then("장바구니에 바나나만 있다") {
                cart shouldContainExactly listOf("banana")
            }
        }
    }
})
```

"바나나" `Then` 을 실행할 때는 `Given` → `When(바나나)` → `Then` 경로만 새 인스턴스에서 실행됩니다. 그래서 사과를 담은 `When` 의 영향을 받지 않습니다.

## 어떻게 상태를 관리할까?

테스트 간 간섭을 피하는 방법은 세 가지입니다.

1. **불변 객체 / 테스트 안에서 생성** (가장 추천): 공유할 필요가 없는 객체는 테스트 블록 안에서 만드세요.
2. **`beforeEach` 로 초기화**: 공유 객체(목, 저장소)는 매 테스트 전후에 초기화합니다. 예) `afterEach { clearMocks(repository) }`
3. **격리 모드 변경**: 위 방법이 어렵다면 `InstancePerLeaf` 또는 `InstancePerRoot` 를 사용합니다. 프로젝트 전체에 적용하려면 `ProjectConfig` 에서 `isolationMode` 를 설정하세요. ([13. 고급 설정](13-advanced-config.md))

> 격리 모드를 바꾸면 Spec 이 여러 번 생성되므로, Spring 컨텍스트처럼 무거운 초기화가 있다면 성능에 영향을 줄 수 있습니다.

## 정리

- 훅: `beforeSpec` / `beforeEach` / `afterEach` / `afterSpec`
- Kotest 의 기본 격리 모드는 **SingleInstance** (JUnit 과 다름!)
- 테스트 간 상태 공유를 피하려면 테스트 안에서 객체를 만들거나, 훅으로 초기화하거나, 격리 모드를 바꾸세요.

---

[← 이전: 06. 예외 검증과 Soft Assertion](06-exceptions-and-soft-assertions.md) | [목차](../README.md) | [다음: 08. 데이터 기반 테스트 →](08-data-driven-testing.md)
