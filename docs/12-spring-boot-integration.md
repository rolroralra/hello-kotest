# 12. Spring Boot 통합 테스트

> 실습 코드: [`spring/`](../src/test/kotlin/com/example/hellokotest/spring)

Kotest 로 Spring Boot 테스트를 작성하려면 **SpringExtension** 이 필요합니다. SpringExtension 은 Spring TestContext 를 Kotest 라이프사이클에 연결해서, JUnit 의 `@ExtendWith(SpringExtension::class)` 와 같은 역할을 합니다.

## 1. SpringExtension 등록

[`ProjectConfig.kt`](../src/test/kotlin/io/kotest/provided/ProjectConfig.kt) 에 한 번만 등록하면 모든 Spec 에 적용됩니다.

```kotlin
package io.kotest.provided

class ProjectConfig : AbstractProjectConfig() {
    override val extensions: List<Extension> = listOf(SpringExtension())
}
```

> Spec 하나에만 적용하고 싶다면 Spec 본문에서 `register(SpringExtension())` 을 호출하거나 클래스에 `@ApplyExtension(SpringExtension::class)` 를 붙입니다. 단, **생성자 주입**은 Spec 인스턴스가 만들어지기 전에 동작해야 하므로 ProjectConfig 에 등록하는 방식을 권장합니다.

## 2. 테스트 계층별 전략

| 테스트 | 애너테이션 | 로딩 범위 | 속도 | 이 프로젝트의 예 |
|--------|-----------|-----------|------|------------------|
| 단위 테스트 | 없음 | 없음 (MockK) | 매우 빠름 | `MemberServiceMockkTest` (10장) |
| 웹 계층 슬라이스 | `@WebMvcTest` | Controller, ControllerAdvice, MVC 설정 | 빠름 | `MemberControllerWebMvcTest` |
| 통합 테스트 | `@SpringBootTest` | 전체 컨텍스트 | 느림 | `MemberServiceIntegrationTest`, `MemberApiFlowTest` |

## 3. @SpringBootTest + 생성자 주입

> [`MemberServiceIntegrationTest.kt`](../src/test/kotlin/com/example/hellokotest/spring/MemberServiceIntegrationTest.kt)

```kotlin
@SpringBootTest
class MemberServiceIntegrationTest(
    private val memberService: MemberService,
    private val memberRepository: InMemoryMemberRepository,
) : FunSpec({

    beforeEach { memberRepository.clear() }

    test("실제 빈으로 회원 가입 후 조회") {
        val saved = memberService.register("홍길동", "hong@example.com")

        memberService.getMember(saved.id).email shouldBe "hong@example.com"
        memberService.getMembers() shouldHaveSize 1
    }
})
```

- SpringExtension 이 Spec 의 **생성자 파라미터에 빈을 주입**합니다. `@Autowired` 가 필요 없습니다.
- 생성자 파라미터는 `FunSpec({ ... })` 람다 안에서 바로 사용할 수 있습니다.
- Spring 컨텍스트는 캐시되므로, 같은 설정의 테스트끼리는 컨텍스트를 재사용합니다.
- 저장소처럼 상태가 남는 빈은 `beforeEach` 에서 초기화하세요.

## 4. @WebMvcTest + @MockkBean

> [`MemberControllerWebMvcTest.kt`](../src/test/kotlin/com/example/hellokotest/spring/MemberControllerWebMvcTest.kt)

웹 계층만 로딩하고, 서비스는 목 빈으로 대체합니다. Spring 의 `@MockitoBean` 대신 **springmockk** 의 `@MockkBean` 을 씁니다.

```kotlin
@WebMvcTest(MemberController::class)
class MemberControllerWebMvcTest : DescribeSpec() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberService: MemberService

    init {
        afterEach { clearMocks(memberService) }

        describe("GET /api/members/{id}") {
            it("회원이 있으면 200 과 회원 정보를 돌려준다") {
                every { memberService.getMember(1L) } returns Member(1L, "홍길동", "hong@example.com")

                mockMvc.get("/api/members/1").andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.name") { value("홍길동") }
                }
            }

            it("회원이 없으면 404 와 에러 메시지를 돌려준다") {
                every { memberService.getMember(99L) } throws MemberNotFoundException(99L)

                mockMvc.get("/api/members/99").andExpect {
                    status { isNotFound() }
                    jsonPath("$.error") { value("회원을 찾을 수 없습니다. id=99") }
                }
            }
        }
    }
}
```

포인트:

- `@MockkBean` 은 **필드**에 붙여야 하므로, 생성자 람다 대신 **`init` 블록** 스타일을 사용합니다. ([04 문서](04-spec-styles.md#생성자-람다-vs-init-블록) 참고)
- `mockMvc.get(...)` / `mockMvc.post(...) { }` 는 Spring 이 제공하는 **MockMvc Kotlin DSL** 입니다. (`org.springframework.test.web.servlet.get`)
- Spring Boot 4 에서 `@WebMvcTest` 의 패키지는 `org.springframework.boot.webmvc.test.autoconfigure` 입니다.

### 요청 본문 검증 (Bean Validation)

```kotlin
it("이메일 형식이 잘못되면 400 을 돌려주고 서비스를 호출하지 않는다") {
    mockMvc.post("/api/members") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"name": "홍길동", "email": "not-an-email"}"""
    }.andExpect {
        status { isBadRequest() }
        jsonPath("$.success") { value(false) }
    }

    verify(exactly = 0) { memberService.register(any(), any()) }
}
```

## 5. 시나리오 테스트 - @SpringBootTest + MockMvc

> [`MemberApiFlowTest.kt`](../src/test/kotlin/com/example/hellokotest/spring/MemberApiFlowTest.kt)

전체 컨텍스트를 띄우고 실제 빈으로 API 흐름을 검증합니다. BehaviorSpec 으로 시나리오를 표현하면 읽기 좋습니다.

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class MemberApiFlowTest(
    private val mockMvc: MockMvc,
    private val memberRepository: InMemoryMemberRepository,
) : BehaviorSpec({

    beforeSpec { memberRepository.clear() }

    Given("새로운 회원이") {
        val body = """{"name": "홍길동", "email": "hong@example.com"}"""

        When("가입하면") {
            Then("201 Created 와 id 1 을 받는다") {
                mockMvc.post("/api/members") {
                    contentType = MediaType.APPLICATION_JSON
                    content = body
                }.andExpect {
                    status { isCreated() }
                    jsonPath("$.data.id") { value(1) }
                }
            }
        }

        When("같은 이메일로 다시 가입하면") {
            Then("409 Conflict 를 받는다") { ... }
        }
    }
})
```

> 이 테스트는 `When` 블록들이 **순서대로 실행되며 상태를 공유한다**는 점(기본 격리 모드 `SingleInstance`)을 이용한 시나리오입니다. 각 테스트를 독립적으로 만들고 싶다면 `beforeEach` 로 초기화하세요.

## 자주 만나는 문제

| 증상 | 원인 / 해결 |
|------|-------------|
| 생성자 파라미터가 있는 Spec 을 만들 수 없다는 오류 | SpringExtension 이 등록되지 않음 → ProjectConfig 의 패키지/클래스명(`io.kotest.provided.ProjectConfig`) 확인 |
| `lateinit property mockMvc has not been initialized` | 필드 주입을 생성자 람다(`FunSpec({ })`) 안에서 사용함 → `init` 블록 스타일로 변경 |
| `@MockkBean` 의 stub 이 다른 테스트에 남음 | `afterEach { clearMocks(...) }` 추가 |

## 정리

- `ProjectConfig` 에 `SpringExtension()` 을 등록하면 생성자 주입이 가능합니다.
- 필드 주입(`@Autowired`, `@MockkBean`)이 필요하면 `init` 블록 스타일을 사용하세요.
- 단위(MockK) → 슬라이스(`@WebMvcTest`) → 통합(`@SpringBootTest`) 순으로 테스트 피라미드를 구성하세요.

---

[← 이전: 11. 코루틴 테스트](11-coroutines.md) | [목차](../README.md) | [다음: 13. 고급 설정과 활용 →](13-advanced-config.md)
