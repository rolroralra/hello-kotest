# 02. 프로젝트 설정

이 문서에서는 Spring Boot 4.1.1 + Kotlin + Gradle Kotlin DSL 프로젝트에 Kotest 를 설정하는 방법을 설명합니다.

## 디렉토리 구조

```
hello-kotest
├── build.gradle.kts
├── settings.gradle.kts
├── docs/                                  # 학습 문서
└── src
    ├── main/kotlin/com/example/hellokotest
    │   ├── basic/        # Calculator, PasswordValidator, Money (순수 Kotlin)
    │   ├── member/       # 회원 API (Controller - Service - Repository)
    │   ├── coroutine/    # suspend 함수 예제
    │   └── common/       # 공통 응답, 예외 처리
    └── test/kotlin
        ├── io/kotest/provided/ProjectConfig.kt   # Kotest 전역 설정
        └── com/example/hellokotest
            ├── basics/       # 03
            ├── styles/       # 04
            ├── assertions/   # 05
            ├── exceptions/   # 06
            ├── lifecycle/    # 07
            ├── datadriven/   # 08
            ├── property/     # 09
            ├── mocking/      # 10
            ├── coroutines/   # 11
            ├── spring/       # 12
            └── advanced/     # 13
```

## build.gradle.kts

```kotlin
plugins {
    // Spring Boot 4.1.1 이 관리하는 Kotlin 버전(2.3.21)과 맞춘다.
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val kotestVersion = "6.2.5"
val mockkVersion = "1.14.11"
val springMockkVersion = "5.0.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Kotest + MockK 를 사용하므로 Mockito 는 제외한다.
        exclude(module = "mockito-core")
        exclude(module = "mockito-junit-jupiter")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")

    // Mocking
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

tasks.withType<Test> {
    useJUnitPlatform()   // Kotest 는 JUnit Platform 엔진으로 실행된다. 반드시 필요!
}
```

### 의존성 설명

| 의존성 | 설명 |
|--------|------|
| `kotest-runner-junit5` | Kotest 테스트 엔진. 이것만 있어도 테스트를 작성하고 실행할 수 있다 |
| `kotest-assertions-core` | `shouldBe`, `shouldContain` 등 핵심 매처 |
| `kotest-property` | 속성 기반 테스트 (`checkAll`, `Arb`) |
| `kotest-extensions-spring` | Spring TestContext 연동 (`SpringExtension`) |
| `mockk` | Kotlin 용 목(mock) 라이브러리 |
| `springmockk` | Spring 테스트에서 `@MockkBean` 사용 |
| `kotlinx-coroutines-test` | 가상 시간 기반 코루틴 테스트 |

### Spring Boot 4 에서 달라진 점

Spring Boot 4 는 모듈이 잘게 나뉘었습니다. 이 프로젝트와 관련된 변경 사항은 다음과 같습니다.

- 웹 스타터 이름: `spring-boot-starter-web` 대신 `spring-boot-starter-webmvc` 사용
- `@WebMvcTest`, `@AutoConfigureMockMvc` 가 `spring-boot-starter-webmvc-test` 로 분리되었고, 패키지도 `org.springframework.boot.webmvc.test.autoconfigure` 로 변경
- Jackson 3 사용: Kotlin 모듈 좌표가 `tools.jackson.module:jackson-module-kotlin`

> Kotest 의 `kotest-extensions-spring` 은 Kotest 6 부터 Kotest 본체와 같은 버전(`io.kotest` 그룹)으로 배포됩니다. 예전 `io.kotest.extensions:kotest-extensions-spring:1.x` 는 Kotest 5 용입니다.

## IntelliJ IDEA 플러그인

IntelliJ 에서 Kotest 를 편하게 쓰려면 **Kotest 플러그인**을 설치하세요.

`Settings > Plugins > Marketplace > "Kotest" 검색 > Install`

플러그인을 설치하면 다음이 가능해집니다.

- 각 테스트 왼쪽의 실행 버튼(▶)으로 개별 테스트 실행
- 테스트 트리를 Spec 구조대로 표시
- 테스트 이름 기준으로 이동

## 테스트 실행 방법

```bash
# 전체 테스트
./gradlew test

# 특정 Spec 클래스만
./gradlew test --tests 'com.example.hellokotest.basics.FirstTest'

# 패키지 단위
./gradlew test --tests 'com.example.hellokotest.styles.*'

# 태그 필터 (13 문서 참고)
./gradlew test -Dkotest.tags='!Slow'
```

실행 결과 리포트는 `build/reports/tests/test/index.html` 에서 볼 수 있습니다.

## 정리

- `kotest-runner-junit5` 의존성 + `useJUnitPlatform()` 이 최소 설정입니다.
- Spring Boot 4 에서는 스타터와 테스트 모듈 이름이 바뀌었으니 주의하세요.
- IntelliJ Kotest 플러그인을 설치하면 개별 테스트 실행이 편해집니다.

---

[← 이전: 01. Kotest 소개](01-introduction.md) | [목차](../README.md) | [다음: 03. 첫 번째 테스트 →](03-first-test.md)
