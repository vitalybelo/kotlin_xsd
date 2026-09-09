package demo.vitos.kotlin_xsd.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Name

@ConfigurationProperties(prefix = "keycloak")
data class KeycloakAppProperties(

    @Name(value = "url") val keycloakUrl: String,
    @Name(value = "realm") val realm: String,
    @Name(value = "client_id") val clientId: String,
    @Name(value = "client_secret") val clientSecret: String
)

@ConfigurationProperties(prefix = "queues")
data class ArtemisQueueAppProperties(

    val xml: QueueProperties,
    val xsd: XsdProperties
) {

    data class XsdProperties(
        val crm: QueueProperties,
        val billing: QueueProperties,
        val dlq: QueueProperties
    )

    data class QueueProperties(
        @Name(value = "in") val queueIn: String,
        @Name(value = "out") val queueOut: String
    )
}

