package com.example.hellokotest.common

import com.example.hellokotest.member.DuplicateEmailException
import com.example.hellokotest.member.MemberNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MemberNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(e: MemberNotFoundException): ApiResponse<Nothing> {
        log.debug("Member not found: {}", e.message)
        return ApiResponse.fail(e.message ?: "회원을 찾을 수 없습니다.")
    }

    @ExceptionHandler(DuplicateEmailException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicate(e: DuplicateEmailException): ApiResponse<Nothing> {
        log.debug("Duplicate email: {}", e.message)
        return ApiResponse.fail(e.message ?: "이미 사용 중인 이메일입니다.")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ApiResponse<Nothing> {
        val message = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        log.debug("Validation failed: {}", message)
        return ApiResponse.fail(message)
    }
}
