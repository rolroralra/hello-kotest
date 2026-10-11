package com.example.hellokotest.basic

/**
 * 비밀번호 규칙 검사기. 위반한 규칙 목록을 돌려준다(비어 있으면 통과).
 * 데이터 기반 테스트(08)와 속성 기반 테스트(09)에서 사용한다.
 */
object PasswordValidator {
    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 20

    enum class Violation(val message: String) {
        TOO_SHORT("비밀번호는 $MIN_LENGTH 자 이상이어야 합니다."),
        TOO_LONG("비밀번호는 $MAX_LENGTH 자 이하여야 합니다."),
        NO_DIGIT("숫자를 하나 이상 포함해야 합니다."),
        NO_UPPERCASE("대문자를 하나 이상 포함해야 합니다."),
        CONTAINS_WHITESPACE("공백을 포함할 수 없습니다."),
    }

    fun validate(password: String): List<Violation> = buildList {
        if (password.length < MIN_LENGTH) add(Violation.TOO_SHORT)
        if (password.length > MAX_LENGTH) add(Violation.TOO_LONG)
        if (password.none { it.isDigit() }) add(Violation.NO_DIGIT)
        if (password.none { it.isUpperCase() }) add(Violation.NO_UPPERCASE)
        if (password.any { it.isWhitespace() }) add(Violation.CONTAINS_WHITESPACE)
    }

    fun isValid(password: String): Boolean = validate(password).isEmpty()
}
