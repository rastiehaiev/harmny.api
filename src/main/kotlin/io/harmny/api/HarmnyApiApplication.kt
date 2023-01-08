package io.harmny.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HarmnyApiApplication

fun main(args: Array<String>) {
    runApplication<HarmnyApiApplication>(*args)
}
