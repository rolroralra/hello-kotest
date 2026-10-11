package com.example.hellokotest.advanced

import com.example.hellokotest.member.InMemoryMemberRepository
import com.example.hellokotest.member.MemberRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * 13-advanced-config.md - Test Factory
 * 인터페이스 계약(contract) 테스트를 한 번 작성하고, 여러 구현체에 재사용한다.
 */
fun memberRepositoryContract(name: String, createRepository: () -> MemberRepository) = funSpec {

    context("$name 은 MemberRepository 계약을 지킨다") {
        test("저장하면 id 가 부여되고 조회할 수 있다") {
            val repository = createRepository()
            val saved = repository.save("홍길동", "hong@example.com")

            repository.findById(saved.id).shouldNotBeNull().name shouldBe "홍길동"
        }

        test("이메일로 조회할 수 있다") {
            val repository = createRepository()
            repository.save("홍길동", "hong@example.com")

            repository.findByEmail("hong@example.com").shouldNotBeNull()
            repository.findByEmail("none@example.com").shouldBeNull()
        }

        test("findAll 은 저장한 순서대로 돌려준다") {
            val repository = createRepository()
            repository.save("a", "a@example.com")
            repository.save("b", "b@example.com")

            repository.findAll() shouldHaveSize 2
            repository.findAll().map { it.name } shouldBe listOf("a", "b")
        }
    }
}

class TestFactoryTest : FunSpec({
    include(memberRepositoryContract("InMemoryMemberRepository") { InMemoryMemberRepository() })
    // 새 구현체(예: JpaMemberRepository)가 생기면 한 줄만 추가하면 된다.
})
