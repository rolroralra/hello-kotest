# 10. MockK 로 목(Mock) 사용하기

> 실습 코드: [`mocking/MemberServiceMockkTest.kt`](../src/test/kotlin/com/example/hellokotest/mocking/MemberServiceMockkTest.kt)

Kotest 자체에는 목(mock) 기능이 없습니다. Kotlin 프로젝트에서는 보통 **[MockK](https://mockk.io)** 를 함께 사용합니다. Mockito 도 쓸 수 있지만 MockK 는 `final` 클래스, `object`, 코루틴(`suspend`), 확장 함수를 자연스럽게 지원합니다.

## 테스트 대상

```kotlin
@Service
class MemberService(private val memberRepository: MemberRepository) {

    fun register(name: String, email: String): Member {
        val normalizedEmail = email.trim().lowercase()
        if (memberRepository.findByEmail(normalizedEmail) != null) {
            throw DuplicateEmailException(normalizedEmail)
        }
        return memberRepository.save(name.trim(), normalizedEmail)
    }

    fun getMember(id: Long): Member =
        memberRepository.findById(id) ?: throw MemberNotFoundException(id)
}
```

서비스 로직만 검증하고 싶으므로, `MemberRepository` 는 목으로 대체합니다. Spring 은 띄우지 않습니다.

## 기본 사용법

```kotlin
val memberRepository = mockk<MemberRepository>()      // 1. 목 생성
val memberService = MemberService(memberRepository)

every { memberRepository.findById(1L) } returns Member(1L, "홍길동", "hong@example.com")   // 2. stub

memberService.getMember(1L).name shouldBe "홍길동"      // 3. 실행 + 검증

verify(exactly = 1) { memberRepository.findById(1L) }  // 4. 호출 검증
```

| 기능 | 문법 |
|------|------|
| 목 생성 | `mockk<T>()` |
| 반환값 지정 | `every { mock.method(args) } returns value` |
| 예외 던지기 | `every { ... } throws Exception()` |
| 동적 응답 | `every { ... } answers { ... }` |
| 아무 인자 | `any()` |
| 호출 검증 | `verify { ... }`, `verify(exactly = n) { ... }` |
| 호출 안 됨 검증 | `verify(exactly = 0) { ... }` 또는 `confirmVerified(mock)` |

> stub 하지 않은 함수를 호출하면 MockK 는 `MockKException` 을 던집니다. 의도하지 않은 호출을 빨리 발견할 수 있는 장점입니다.

## BehaviorSpec 과 함께 쓰기

```kotlin
class MemberServiceMockkTest : BehaviorSpec({

    val memberRepository = mockk<MemberRepository>()
    val memberService = MemberService(memberRepository)

    afterEach { clearMocks(memberRepository) }   // 테스트 사이에 stub / 호출 기록 초기화

    Given("회원 가입") {
        When("이미 사용 중인 이메일이면") {
            Then("DuplicateEmailException 이 발생하고 저장하지 않는다") {
                every { memberRepository.findByEmail("hong@example.com") } returns
                    Member(1L, "홍길동", "hong@example.com")

                shouldThrow<DuplicateEmailException> {
                    memberService.register("다른사람", "hong@example.com")
                }
                verify(exactly = 0) { memberRepository.save(any(), any()) }
            }
        }
    }
})
```

> **주의**: [07 문서](07-lifecycle-and-isolation.md)에서 봤듯이 Kotest 기본 격리 모드에서는 Spec 본문의 목 객체를 모든 테스트가 공유합니다. `afterEach { clearMocks(...) }` 를 빠뜨리면 이전 테스트의 stub 이나 호출 횟수가 다음 테스트에 섞입니다.

## slot - 전달된 인자 캡처

메서드에 **어떤 값이 전달되었는지** 확인하고 싶을 때 사용합니다.

```kotlin
val nameSlot = slot<String>()
val emailSlot = slot<String>()
every { memberRepository.findByEmail(any()) } returns null
every { memberRepository.save(capture(nameSlot), capture(emailSlot)) } answers {
    Member(1L, nameSlot.captured, emailSlot.captured)
}

val member = memberService.register("  홍길동 ", " Hong@Example.COM ")

emailSlot.captured shouldBe "hong@example.com"   // 정규화된 이메일이 전달되었는지 확인
```

## relaxed mock

모든 함수가 기본값(0, 빈 문자열, 빈 리스트 등)을 돌려주는 목입니다. stub 이 필요 없는 의존성에 편리합니다.

```kotlin
val relaxed = mockk<MemberRepository>(relaxed = true)
MemberService(relaxed).getMembers() shouldBe emptyList()
```

> 남용하면 "stub 을 깜빡했는데 테스트는 통과" 하는 상황이 생길 수 있으니 필요한 곳에만 쓰세요.

## 코루틴(suspend) 함수

`suspend` 함수는 앞에 `co` 를 붙인 함수를 씁니다. ([11. 코루틴 테스트](11-coroutines.md) 참고)

```kotlin
coEvery { client.getRate("USD", "KRW") } returns BigDecimal("1400")
coVerify(exactly = 1) { client.getRate("USD", "KRW") }
```

## 목을 언제 쓸까?

- **써야 할 때**: 외부 API, 시간, 랜덤 등 제어하기 어려운 의존성 / 아직 구현되지 않은 의존성
- **피해야 할 때**: 단순한 값 객체(data class), 직접 만들기 쉬운 객체 / 모든 내부 협력 객체(구현 세부 사항에 테스트가 묶여 리팩토링이 어려워짐)

## 정리

- `mockk<T>()` → `every { } returns` → `verify { }` 흐름을 기억하세요.
- 공유 목은 `afterEach { clearMocks(...) }` 로 초기화합니다.
- `slot` 으로 인자를 캡처하고, `suspend` 함수는 `coEvery` / `coVerify` 를 씁니다.

---

[← 이전: 09. 속성 기반 테스트](09-property-based-testing.md) | [목차](../README.md) | [다음: 11. 코루틴 테스트 →](11-coroutines.md)
