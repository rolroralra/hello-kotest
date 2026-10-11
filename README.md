# hello-kotest

Kotlin 테스트 프레임워크 **[Kotest](https://kotest.io)** 를 처음 접하는 분들을 위한 입문 가이드와 실습 프로젝트입니다.
기본 문법부터 MockK, 코루틴, Spring Boot 통합 테스트까지 단계별 문서와, 문서와 1:1 로 대응하는 **실행 가능한 테스트 코드**로 구성되어 있습니다.

## 기술 스택

| 항목 | 버전 |
|------|------|
| Kotlin | 2.3.21 |
| Spring Boot | 4.1.1 |
| Kotest | 6.2.5 |
| MockK / springmockk | 1.14.11 / 5.0.1 |
| JDK | 21 |
| Build | Gradle 9.7.1 (Kotlin DSL) |

## 빠른 시작

```bash
git clone <this-repo>
cd hello-kotest

# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests 'com.example.hellokotest.basics.FirstTest'
```

- JDK 21 이 필요합니다.
- IntelliJ IDEA 를 사용한다면 **Kotest 플러그인**을 설치하세요. 테스트 옆의 실행 버튼으로 개별 테스트를 실행할 수 있습니다.
- 테스트 리포트: `build/reports/tests/test/index.html`

## 학습 순서

| # | 문서 | 내용 | 실습 코드 |
|---|------|------|-----------|
| 01 | [Kotest 소개](docs/01-introduction.md) | Kotest 란? JUnit 과 비교 | - |
| 02 | [프로젝트 설정](docs/02-project-setup.md) | Gradle Kotlin DSL, Spring Boot 4 설정 | [`build.gradle.kts`](build.gradle.kts) |
| 03 | [첫 번째 테스트](docs/03-first-test.md) | StringSpec, `shouldBe`, 실패 메시지 읽기 | [`basics/`](src/test/kotlin/com/example/hellokotest/basics) |
| 04 | [테스트 스타일(Spec)](docs/04-spec-styles.md) | FunSpec, BehaviorSpec, DescribeSpec 등 | [`styles/`](src/test/kotlin/com/example/hellokotest/styles) |
| 05 | [검증(Assertions)과 매처](docs/05-assertions.md) | 문자열/숫자/컬렉션 매처, Inspector | [`assertions/`](src/test/kotlin/com/example/hellokotest/assertions) |
| 06 | [예외 검증과 Soft Assertion](docs/06-exceptions-and-soft-assertions.md) | `shouldThrow`, `assertSoftly`, `withClue`, 커스텀 매처 | [`exceptions/`](src/test/kotlin/com/example/hellokotest/exceptions) |
| 07 | [라이프사이클과 격리 모드](docs/07-lifecycle-and-isolation.md) | `beforeEach`, `afterSpec`, IsolationMode | [`lifecycle/`](src/test/kotlin/com/example/hellokotest/lifecycle) |
| 08 | [데이터 기반 테스트](docs/08-data-driven-testing.md) | `withData` (파라미터화 테스트) | [`datadriven/`](src/test/kotlin/com/example/hellokotest/datadriven) |
| 09 | [속성 기반 테스트](docs/09-property-based-testing.md) | `checkAll`, `forAll`, `Arb`, Shrinking | [`property/`](src/test/kotlin/com/example/hellokotest/property) |
| 10 | [MockK 로 목(Mock) 사용하기](docs/10-mocking-with-mockk.md) | `mockk`, `every`, `verify`, `slot` | [`mocking/`](src/test/kotlin/com/example/hellokotest/mocking) |
| 11 | [코루틴 테스트](docs/11-coroutines.md) | suspend 테스트, 가상 시간, `eventually` | [`coroutines/`](src/test/kotlin/com/example/hellokotest/coroutines) |
| 12 | [Spring Boot 통합 테스트](docs/12-spring-boot-integration.md) | SpringExtension, `@SpringBootTest`, `@WebMvcTest`, `@MockkBean` | [`spring/`](src/test/kotlin/com/example/hellokotest/spring) |
| 13 | [고급 설정과 활용](docs/13-advanced-config.md) | config, 태그, ProjectConfig, Test Factory | [`advanced/`](src/test/kotlin/com/example/hellokotest/advanced) |

- **입문 (01~06)**: Kotest 로 테스트를 작성하고 검증하는 기본기
- **중급 (07~09)**: 테스트 구조화와 효율적인 테스트 작성법
- **응용 (10~13)**: 실무에서 쓰는 목, 코루틴, Spring 연동, 프로젝트 설정

## 프로젝트 구조

```
src
├── main/kotlin/com/example/hellokotest
│   ├── basic/         # Calculator, PasswordValidator, Money - 순수 Kotlin 테스트 대상
│   ├── member/        # 회원 REST API (Controller - Service - Repository)
│   ├── coroutine/     # suspend 함수 테스트 대상 (PriceConverter)
│   └── common/        # 공통 응답(ApiResponse), 예외 처리
└── test/kotlin
    ├── io/kotest/provided/ProjectConfig.kt   # Kotest 전역 설정 (SpringExtension 등록)
    └── com/example/hellokotest/              # 문서별 실습 테스트
```

### 예제 API

| Method | URL | 설명 |
|--------|-----|------|
| `POST` | `/api/members` | 회원 가입 (`{"name": "...", "email": "..."}`) |
| `GET` | `/api/members/{id}` | 회원 조회 |
| `GET` | `/api/members` | 회원 목록 |

```bash
./gradlew bootRun
curl -X POST localhost:8080/api/members -H 'Content-Type: application/json' \
  -d '{"name": "홍길동", "email": "hong@example.com"}'
```

## 이렇게 학습하세요

1. 문서를 순서대로 읽으면서 연결된 테스트 코드를 IDE 에서 함께 열어 보세요.
2. 테스트를 직접 실행하고, **기대값을 바꿔 일부러 실패시켜** 실패 메시지를 확인해 보세요.
3. 각 문서의 **연습 문제**를 풀어 테스트를 추가해 보세요.

## 참고 자료

- [Kotest 공식 문서](https://kotest.io/docs/framework/framework.html)
- [Kotest GitHub](https://github.com/kotest/kotest)
- [MockK 공식 문서](https://mockk.io)
- [springmockk](https://github.com/Ninja-Squad/springmockk)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
