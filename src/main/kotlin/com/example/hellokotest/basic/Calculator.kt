package com.example.hellokotest.basic

/**
 * 가장 단순한 테스트 대상. 03~06 문서에서 사용한다.
 */
class Calculator {
    fun add(a: Int, b: Int): Int = a + b

    fun subtract(a: Int, b: Int): Int = a - b

    fun multiply(a: Int, b: Int): Int = a * b

    fun divide(a: Int, b: Int): Int {
        if (b == 0) throw ArithmeticException("0으로 나눌 수 없습니다.")
        return a / b
    }
}
