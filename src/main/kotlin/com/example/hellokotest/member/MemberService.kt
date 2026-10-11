package com.example.hellokotest.member

import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {

    fun register(name: String, email: String): Member {
        val normalizedEmail = email.trim().lowercase()
        if (memberRepository.findByEmail(normalizedEmail) != null) {
            throw DuplicateEmailException(normalizedEmail)
        }
        return memberRepository.save(name.trim(), normalizedEmail)
    }

    fun getMember(id: Long): Member =
        memberRepository.findById(id) ?: throw MemberNotFoundException(id)

    fun getMembers(): List<Member> = memberRepository.findAll()
}
