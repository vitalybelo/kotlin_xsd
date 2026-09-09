package demo.vitos.kotlin_xsd.config

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig(
    val properties: KeycloakAppProperties
) {

    @Bean
    @Qualifier("keycloakAdmin")
    fun keycloakAdmin(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(properties.keycloakUrl)
            .realm(properties.realm)
            .clientId(properties.clientId)
            .clientSecret(properties.clientSecret)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .build()
    }

    @Bean
    fun realmResource(): RealmResource {
        return keycloakAdmin().realm(properties.realm)
    }
}