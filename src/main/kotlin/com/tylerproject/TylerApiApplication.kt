package com.tylerproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class TylerApiApplication

fun main(args: Array<String>) {
    runApplication<TylerApiApplication>(*args)
}
