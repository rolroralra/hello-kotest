package com.example.hellokotest.member

import com.example.hellokotest.common.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class RegisterMemberRequest(
    @field:NotBlank
    @field:Size(max = 30)
    val name: String,
    @field:NotBlank
    @field:Email
    val email: String,
)

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterMemberRequest): ApiResponse<Member> =
        ApiResponse.ok(memberService.register(request.name, request.email))

    @GetMapping("/{id}")
    fun getMember(@PathVariable id: Long): ApiResponse<Member> =
        ApiResponse.ok(memberService.getMember(id))

    @GetMapping
    fun getMembers(): ApiResponse<List<Member>> =
        ApiResponse.ok(memberService.getMembers())
}
