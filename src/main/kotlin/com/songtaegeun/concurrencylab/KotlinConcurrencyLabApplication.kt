package com.songtaegeun.concurrencylab

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinConcurrencyLabApplication

fun main(args: Array<String>) {
	runApplication<KotlinConcurrencyLabApplication>(*args)
}
