package demo.vitos.kotlin_xsd.service

import demo.vitos.kotlin_xsd.broker.ArtemisXsdSender
import demo.vitos.kotlin_xsd.enums.EventType
import demo.vitos.kotlin_xsd.logging.Log
import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class KeycloakUserService(
    private val realmResource: RealmResource,
    private val keycloakUserMapping: KeycloakUserMapping,
    private val artemisXsdSender: ArtemisXsdSender,
) {

    companion object: Log()


    /**
     * Ищет в Keycloak пользователя по регистрационному имени
     * @param username имя пользователя
     * @return найденная сущность пользователя, или null
     */
    fun findKeycloakUser(username: String): ResponseEntity<UserRepresentation> {
        searchKeycloakUserByUsername(username)?.let { user ->
            return ResponseEntity(user, HttpStatus.OK)
        }
        return ResponseEntity.notFound().build()
    }


    /**
     * Выполняет создание новой или изменение существующей сущности пользователя Keycloak
     * @param userMap карта я новыми полями учетных данных пользователя
     * @return вновь созданную или обновленную сущность
     */
    fun createUpdateKeycloakUser(userMap: Map<String, Any?>): ResponseEntity<UserRepresentation> {

        if (userMap.isEmpty()) return ResponseEntity.badRequest().build()
        val username = userMap["username"] as? String ?: return ResponseEntity.badRequest().build()

        // ищем пользователя в Keycloak
        // если пользователь не найден, создаем новую сущность активного пользователя
        var isCreate = false
        val userRepresentation = searchKeycloakUserByUsername(username) ?: UserRepresentation()
        if (userRepresentation.id == null) {
            userRepresentation.isEnabled = true
            isCreate = true
        }

        // обновляем поля учётной сущности переданными в метод данными, фиксируем количество изменений
        val updatedFieldsCount =
            keycloakUserMapping.updateUserRepresentationFields(userRepresentation, userMap)

        if (updatedFieldsCount > 0) {
            if (isCreate) {
                val createdUserRepresentation = createKeycloakUser(userRepresentation)
                if (createdUserRepresentation != null) {
                    artemisXsdSender.sendUserEvent(createdUserRepresentation, EventType.CREATED)
                    return ResponseEntity.ok(createdUserRepresentation)
                } else {
                    return ResponseEntity.internalServerError().build()
                }
            } else {
                realmResource.users().get(userRepresentation.id).update(userRepresentation)
                logger.infoM("Keycloak user updated successfully [username = $username]")
                artemisXsdSender.sendUserEvent(userRepresentation, EventType.UPDATED)
                return ResponseEntity.ok(userRepresentation)
            }
        } else {
            logger.infoM("Nothing to update for user = $username")
            return ResponseEntity.noContent().build()
        }
    }


    /**
     * Выполняет создание нового пользователя в keycloak
     * @param userRepresentation первично заполненная сущность (минимум username)
     * @return сущность созданного пользователя
     */
    private fun createKeycloakUser(userRepresentation: UserRepresentation): UserRepresentation? {

        var response: Response? = null
        val username = userRepresentation.username ?: return null
        try {
            response = realmResource.users().create(userRepresentation)
            if (response.status == 201) {
                val userId = CreatedResponseUtil.getCreatedId(response)
                if (!userId.isNullOrBlank()) {
                    logger.infoM("Keycloak user created successfully [username = $username]")
                    return realmResource.users()?.get(userId)?.toRepresentation()
                }
            } else {
                logger.infoM("Keycloak user [username = $username] no created >>>> status = ${response.status}")
            }
        } catch (ex: Exception) {
            logger.errorM("Failed to create keycloak user", ex)
        } finally {
            response?.close()
        }
        return null
    }


    /**
     * Выполняет поиск пользователя в Keycloak по имени (username)
     * @param username имя пользователя (login name)
     * @return найденная сущность пользователя, или null
     */
    private fun searchKeycloakUserByUsername(username: String): UserRepresentation? {
        try {
            val foundUser = realmResource
                .users()
                .searchByUsername(username, true)
                .firstOrNull()

            if (foundUser != null) {
                logger.infoM("Keycloak user found >>>> username = ${foundUser.username}, id = [${foundUser.id}]")
                return foundUser
            }

        } catch (ex: Exception) {
            logger.errorM("User not found >>>> message = ${ex.message}, cause = ${ex.cause}")
        }
        return null
    }


}