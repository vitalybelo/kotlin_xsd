package demo.vitos.kotlin_xsd.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonRootName
import demo.vitos.kotlin_xsd.enums.EventType
import org.keycloak.representations.idm.UserRepresentation

@JsonRootName("UserEvent")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UserEvent(

    val eventType: String,
    val userId: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val twoFactorMethod: String,
    val enable: Boolean
) {

    companion object {

        fun fromKeycloakRepresentation(user: UserRepresentation, eventType: EventType): UserEvent {

            val twoFactorMethod = user.attributes["required_2FA"]?.firstOrNull() ?: ""
            val phone = user.attributes["phone"]?.firstOrNull() ?: ""

            return UserEvent(
                eventType = eventType.name,
                userId = user.id ?: "",
                username = user.username ?: "noname",
                email = user.email ?: "",
                firstName = user.firstName ?: "",
                lastName = user.lastName ?: "",
                twoFactorMethod = twoFactorMethod,
                phone = phone,
                enable = user.isEnabled ?: false
            )
        }
    }
}