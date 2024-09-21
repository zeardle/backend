package com.zulfen.zeardle

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ZeardleApplication

fun main(args: Array<String>) {
	runApplication<ZeardleApplication>(*args)
}
