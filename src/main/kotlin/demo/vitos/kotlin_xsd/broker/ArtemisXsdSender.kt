package demo.vitos.kotlin_xsd.broker

import demo.vitos.kotlin_xsd.config.ArtemisQueueAppProperties
import demo.vitos.kotlin_xsd.dto.UserEvent
import demo.vitos.kotlin_xsd.enums.EventType
import demo.vitos.kotlin_xsd.logging.Log
import demo.vitos.kotlin_xsd.utils.toPrettyXml
import demo.vitos.kotlin_xsd.validator.XsdValidator
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.jms.JmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import tools.jackson.dataformat.xml.XmlMapper

/**
 * Класс отправки xml сообщения в очередь через брокер Artemis
 * @author Belotserkovskii Vitalii (c)
 */
@Service
class ArtemisXsdSender(
    private val xmlMapper: XmlMapper,
    private val jmsTemplate: JmsTemplate,
    private val xsdValidator: XsdValidator,
    private val properties: ArtemisQueueAppProperties
) {

    companion object: Log()


    fun sendUserEvent(
        userRepresentation: UserRepresentation,
        eventType: EventType
    ) {
        sendXmlMessage(userRepresentation, eventType)
        sendXsdMessage(userRepresentation, eventType, properties.xsd.crm.queueOut)
        sendXsdMessage(userRepresentation, eventType, properties.xsd.billing.queueOut)
    }

    /**
     * На основе переданной сущности учетных данных пользователя создает класс данных пользовательского
     * события. Затем формирует из класса данных xml для отправки в интеграцию, и выполняет отправку
     * сообщения через брокер Artemis через очередь.
     * @param userRepresentation сущность пользователя
     * @param eventType тип пользовательского события
     */
    fun sendXmlMessage(
        userRepresentation: UserRepresentation,
        eventType: EventType
    ) {
        val username = getUsername(userRepresentation)
        try {
            val xmlPayload = prepareXmlPayload(userRepresentation, eventType)

            jmsTemplate.convertAndSend(properties.xml.queueOut, xmlPayload)
            logger.infoM("User event successfully sent :: ${eventType.description} :: [username = $username]")

        } catch (ex: Exception) {
            logger.errorM("Sending user event [username = $username] failed", ex)
        }
    }


    /**
     * На основе переданной сущности учетных данных пользователя создает класс данных пользовательского
     * события. Затем формирует из класса данных xml для отправки в интеграцию, и выполняет отправку
     * сообщения через брокер Artemis через очередь.
     * @param userRepresentation сущность пользователя
     * @param eventType тип пользовательского события
     * @param queueName название очереди отправки
     *
     */
    fun sendXsdMessage(
        userRepresentation: UserRepresentation,
        eventType: EventType,
        queueName: String,
    ) {
        val username = getUsername(userRepresentation)
        try {
            val xmlPayload = prepareXmlPayload(userRepresentation, eventType)
            xsdValidator.validate(xmlPayload)

            jmsTemplate.convertAndSend(queueName, xmlPayload)
            logger.infoM("Message validated by XSD successfully sent to CRM/Billing for user = $username")

        } catch (ex: Exception) {
            when(ex) {
                is JmsException -> {
                    logger.errorM("Sending message failed for user = $username", ex)
                }
                else -> {
                    // отправляем в очередь DLQ
                    logger.errorM("XSD validation failed. Message routed to DLQ for user = $username", ex)
                    val xmlPayload = prepareXmlPayload(userRepresentation, eventType)
                    jmsTemplate.convertAndSend(properties.xsd.dlq.queueOut, xmlPayload)
                }
            }
        }
    }


    private fun getUsername(userRepresentation: UserRepresentation): String = userRepresentation.username ?: "noname"

    private fun prepareXmlPayload(userRepresentation: UserRepresentation, eventType: EventType): String {
        val userEvent = UserEvent.fromKeycloakRepresentation(userRepresentation, eventType)
        val xmlPayload = xmlMapper.writeValueAsString(userEvent)
        logger.debugM("Prepared xml payload for user event: ${xmlPayload.toPrettyXml()}")
        return xmlPayload
    }

}