package demo.vitos.kotlin_xsd.service

import demo.vitos.kotlin_xsd.logging.Log
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import org.springframework.beans.BeanWrapperImpl


@Service
class KeycloakUserMapping {

    companion object: Log()


    /**
     * Выполняет обновление сущности пользователя полями, переданными в метод.
     *
     * @param userRepresentation существующая сущность пользователя
     * @param userRepresentationMap карта новых учётных полей пользователя
     * @return количество выполненных изменений
     */

    fun updateUserRepresentationFields(
        userRepresentation: UserRepresentation,
        userRepresentationMap: Map<String, Any?>
    ): Int {

        var count = 0
        // BeanWrapper оборачивает наш объект и дает доступ к его свойствам
        val wrapper = BeanWrapperImpl(userRepresentation)

        userRepresentationMap.forEach { (key, value) ->

            if (key == "id") return@forEach

            // Проверяем, есть ли у класса такое поле (включая родительские) и сеттер для него
            if (wrapper.isWritableProperty(key)) {
                // Это стандартное поле Keycloak (например: username, firstName, email)
                try {
                    if (key == "attributes") {
                        @Suppress("UNCHECKED_CAST")
                        val attributes = value as? Map<String, List<String>> ?: return@forEach
                        handleAttributes(userRepresentation, attributes)
                        logger.infoM("Attributes map updated [key = $key, value = $value]")
                    } else {
                        wrapper.setPropertyValue(key, value)
                        logger.infoM("Standard field updated [key = $key, value = $value]")
                    }
                    count++
                } catch (ex: Exception) {
                    logger.errorM("Failed to update standard field = [$key] >>>> [${ex.message}]")
                }
            } else {
                // Этого поля нет в классе, считаем кастомной бизнес-информацией и кладем в attributes
                try {
                    if (userRepresentation.attributes == null) {
                        userRepresentation.attributes = mutableMapOf()
                    }
                    userRepresentation.attributes[key] = listOf(value.toString())
                    logger.infoM("Custom attribute updated [key = $key, value = $value]")
                    count++

                } catch (ex: Exception) {
                    logger.errorM("Failed to update custom attribute = [$key]", ex)
                }
            }
        }

        return count
    }


    private fun handleAttributes(
        userRepresentation: UserRepresentation,
        attributes: Map<String, List<String>?>) {

        if (userRepresentation.attributes == null) {
            userRepresentation.attributes = mutableMapOf()
        }
        attributes.forEach { (key, value) ->
            if (value != null) {
                userRepresentation.attributes?.put(key, value)
            }
        }
    }
}