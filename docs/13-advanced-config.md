# 13. 고급 설정과 활용

> 실습 코드: [`advanced/TagsAndConfigTest.kt`](../src/test/kotlin/com/example/hellokotest/advanced/TagsAndConfigTest.kt), [`advanced/TestFactoryTest.kt`](../src/test/kotlin/com/example/hellokotest/advanced/TestFactoryTest.kt), [`ProjectConfig.kt`](../src/test/kotlin/io/kotest/provided/ProjectConfig.kt)

## 1. 테스트별 설정 - config()

모든 스타일에서 테스트 이름 뒤에 `.config(...)` 를 붙여 개별 테스트의 동작을 바꿀 수 있습니다.

```kotlin
test("timeout - 지정 시간 안에 끝나야 한다").config(timeout = 1.seconds) { ... }

test("invocations - 같은 테스트를 여러 번 실행한다").config(invocations = 3) { ... }

test("enabled = false 로 비활성화").config(enabled = false) { ... }

test("enabledIf - 조건부 실행").config(enabledIf = { System.getenv("CI") == null }) { ... }
```

| 옵션 | 설명 |
|------|------|
| `enabled` | `false` 면 건너뜀 |
| `enabledIf` | 람다 결과가 `true` 일 때만 실행 |
| `timeout` | 제한 시간 초과 시 실패 |
| `invocations` | 반복 실행 횟수 (간헐적 실패 재현 등) |
| `tags` | 태그 지정 |
| `coroutineTestScope` | 가상 시간 사용 ([11 문서](11-coroutines.md)) |

## 2. 테스트 비활성화 / 집중 실행

```kotlin
xtest("x 접두사로 비활성화") { ... }          // xcontext, xdescribe, xit 등도 있음
test("!느낌표로 시작하면 비활성화") { ... }

test("f:focus - 이 테스트만 실행") { ... }     // Spec 안에서 f: 로 시작하는 최상위 테스트만 실행된다
```

> `f:` 는 디버깅할 때만 잠깐 쓰고 **절대 커밋하지 마세요.** 다른 테스트가 모두 건너뛰어집니다.

## 3. 태그(Tag)

느린 테스트, 통합 테스트 등을 태그로 구분해 골라서 실행할 수 있습니다.

```kotlin
object Slow : Tag()
object Fast : Tag()

class TagsAndConfigTest : FunSpec({
    tags(Fast)                                              // Spec 전체에 태그

    test("태그를 붙인 테스트").config(tags = setOf(Slow)) { ... }   // 개별 테스트에 태그
})
```

실행할 때 `kotest.tags` 시스템 프로퍼티로 태그 식을 넘깁니다.

```bash
./gradlew test -Dkotest.tags='Fast'              # Fast 태그만
./gradlew test -Dkotest.tags='!Slow'             # Slow 제외
./gradlew test -Dkotest.tags='Fast & !Slow'      # 조합 (&, |, !, 괄호)
```

Gradle 은 시스템 프로퍼티를 테스트 JVM 으로 자동 전달하지 않으므로, `build.gradle.kts` 에 다음 설정이 필요합니다.

```kotlin
tasks.withType<Test> {
    useJUnitPlatform()
    System.getProperty("kotest.tags")?.let { systemProperty("kotest.tags", it) }
}
```

## 4. 프로젝트 전역 설정 - ProjectConfig

Kotest 6 는 **`io.kotest.provided.ProjectConfig`** 라는 정확한 이름의 클래스를 찾아 전역 설정으로 사용합니다. (Kotest 5 처럼 클래스패스 전체를 스캔하지 않습니다.)

```kotlin
package io.kotest.provided

class ProjectConfig : AbstractProjectConfig() {
    override val extensions: List<Extension> = listOf(SpringExtension())
    override val timeout: Duration = 30.seconds
}
```

다른 위치에 두고 싶다면 시스템 프로퍼티 `kotest.framework.config.fqn` 으로 클래스 이름을 지정할 수 있습니다.

자주 쓰는 설정:

| 프로퍼티 | 설명 |
|----------|------|
| `extensions` | 전역 Extension (SpringExtension 등) |
| `isolationMode` | 기본 격리 모드 ([07 문서](07-lifecycle-and-isolation.md)) |
| `timeout` | 테스트 기본 제한 시간 |
| `specExecutionMode` / `testExecutionMode` | Spec / 테스트 병렬 실행 |
| `globalAssertSoftly` | 모든 테스트를 `assertSoftly` 로 실행 |
| `failOnEmptyTestSuite` | 실행된 테스트가 없으면 실패 |
| `retries` | 실패 시 재시도 횟수 |

또는 `src/test/resources/kotest.properties` 파일에 `kotest.framework.timeout=30000` 처럼 적을 수도 있습니다.

## 5. Test Factory - 테스트 재사용

같은 테스트를 여러 Spec / 구현체에 재사용하고 싶을 때 사용합니다. 인터페이스의 **계약(contract) 테스트**에 특히 유용합니다.

```kotlin
fun memberRepositoryContract(name: String, createRepository: () -> MemberRepository) = funSpec {
    context("$name 은 MemberRepository 계약을 지킨다") {
        test("저장하면 id 가 부여되고 조회할 수 있다") {
            val repository = createRepository()
            val saved = repository.save("홍길동", "hong@example.com")

            repository.findById(saved.id).shouldNotBeNull().name shouldBe "홍길동"
        }
        // ...
    }
}

class TestFactoryTest : FunSpec({
    include(memberRepositoryContract("InMemoryMemberRepository") { InMemoryMemberRepository() })
    // 새 구현체(예: JpaMemberRepository)가 생기면 한 줄만 추가하면 된다.
})
```

- `funSpec { }`, `behaviorSpec { }`, `describeSpec { }` 등 각 스타일별 팩토리 함수가 있습니다.
- `include(...)` 로 Spec 에 포함시킵니다. 팩토리는 **같은 스타일의 Spec** 에만 포함할 수 있습니다.

## 6. 더 알아보기

| 주제 | 설명 |
|------|------|
| Extension / Listener 직접 만들기 | `BeforeTestListener`, `TestCaseExtension` 등을 구현해 공통 동작 추가 |
| `kotest-assertions-json` | JSON 문자열을 구조적으로 비교 (`shouldEqualJson`) |
| `kotest-extensions-testcontainers` | Testcontainers 로 실제 DB/Redis 를 띄워 테스트 |
| Kover | Kotlin 코드 커버리지 측정 |

공식 문서: <https://kotest.io/docs/framework/framework.html>

## 정리

- `.config(...)` 로 테스트별 timeout, 반복, 활성화 조건, 태그를 설정합니다.
- 태그 + `-Dkotest.tags` 로 실행할 테스트를 고릅니다.
- 전역 설정은 `io.kotest.provided.ProjectConfig` 에 둡니다.
- Test Factory 로 계약 테스트를 재사용합니다.

수고하셨습니다! 이제 직접 만든 프로젝트에 Kotest 를 적용해 보세요.

---

[← 이전: 12. Spring Boot 통합 테스트](12-spring-boot-integration.md) | [다음: 목차(README)로 돌아가기 →](../README.md)
