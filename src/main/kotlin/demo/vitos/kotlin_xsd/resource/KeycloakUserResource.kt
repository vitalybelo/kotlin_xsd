package demo.vitos.kotlin_xsd.resource

import demo.vitos.kotlin_xsd.logging.Log
import demo.vitos.kotlin_xsd.service.KeycloakUserService
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/keycloak")
class KeycloakUserResource(
    private val keycloakUserService: KeycloakUserService
) {

    companion object: Log()

    @GetMapping("/user")
    fun findKeycloakUser(
        @RequestParam(value = "username", required = true) username: String,
    ): ResponseEntity<UserRepresentation> {
        logger.infoM("Request received for user = $username")
        return keycloakUserService.findKeycloakUser(username)
    }


    @PostMapping("/user")
    fun createUpdateUser(
        @RequestBody(required = true) user: Map<String, Any?>
    ): ResponseEntity<UserRepresentation> {
        logger.infoM("Request received for user map = $user")
        return keycloakUserService.createUpdateKeycloakUser(user)
    }

}