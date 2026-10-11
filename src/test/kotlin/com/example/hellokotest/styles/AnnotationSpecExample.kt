package com.example.hellokotest.styles

import com.example.hellokotest.basic.Calculator
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

/**
 * 04-spec-styles.md - AnnotationSpec
 * JUnit 에서 넘어올 때 가장 익숙한 형태. 기존 테스트를 옮길 때 유용하다.
 */
class AnnotationSpecExample : AnnotationSpec() {

    private lateinit var calculator: Calculator

    @BeforeEach
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun addition() {
        calculator.add(1, 1) shouldBe 2
    }

    @Test
    fun multiplication() {
        calculator.multiply(3, 3) shouldBe 9
    }
}
