package com.example.hellokotest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelloKotestApplication

fun main(args: Array<String>) {
    runApplication<HelloKotestApplication>(*args)
}
