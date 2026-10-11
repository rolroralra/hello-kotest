package com.example.hellokotest.member

import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 학습용 인메모리 저장소. DB 없이 Spring 통합 테스트를 실습하기 위해 사용한다.
 */
@Repository
class InMemoryMemberRepository : MemberRepository {

    private val store = ConcurrentHashMap<Long, Member>()
    private val sequence = AtomicLong(0)

    override fun save(name: String, email: String): Member {
        val member = Member(id = sequence.incrementAndGet(), name = name, email = email)
        store[member.id] = member
        return member
    }

    override fun findById(id: Long): Member? = store[id]

    override fun findByEmail(email: String): Member? = store.values.firstOrNull { it.email == email }

    override fun findAll(): List<Member> = store.values.sortedBy { it.id }

    fun clear() {
        store.clear()
        sequence.set(0)
    }
}
