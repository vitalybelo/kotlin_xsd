package demo.vitos.kotlin_xsd

import demo.vitos.kotlin_xsd.config.ArtemisQueueAppProperties
import demo.vitos.kotlin_xsd.config.KeycloakAppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    KeycloakAppProperties::class,
    ArtemisQueueAppProperties::class
)
class KotlinXsdApplication

fun main(args: Array<String>) {
    runApplication<KotlinXsdApplication>(*args)
}
