package com.example.hellokotest.member

interface MemberRepository {
    fun save(name: String, email: String): Member

    fun findById(id: Long): Member?

    fun findByEmail(email: String): Member?

    fun findAll(): List<Member>
}
