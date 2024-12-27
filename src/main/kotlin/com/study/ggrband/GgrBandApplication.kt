package com.study.ggrband

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
class GgrBandApplication

fun main(args: Array<String>) {
    runApplication<GgrBandApplication>(*args)
}