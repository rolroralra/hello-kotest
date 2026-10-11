package com.example.hellokotest.common

/**
 * 모든 API 응답에 사용하는 공통 응답 형식.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun fail(message: String): ApiResponse<Nothing> = ApiResponse(success = false, error = message)
    }
}
