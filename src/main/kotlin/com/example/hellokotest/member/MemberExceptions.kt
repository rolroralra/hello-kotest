package com.example.hellokotest.member

class MemberNotFoundException(id: Long) : RuntimeException("회원을 찾을 수 없습니다. id=$id")

class DuplicateEmailException(email: String) : RuntimeException("이미 사용 중인 이메일입니다. email=$email")
