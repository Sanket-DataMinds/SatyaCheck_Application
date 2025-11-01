package com.satyacheck.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SatyacheckBackendApplication

fun main(args: Array<String>) {
    runApplication<SatyacheckBackendApplication>(*args)
}