package demo.vitos.kotlin_xsd.broker

import demo.vitos.kotlin_xsd.dto.UserEvent
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import demo.vitos.kotlin_xsd.logging.Log
import demo.vitos.kotlin_xsd.utils.toPrettyXml
import tools.jackson.dataformat.xml.XmlMapper


@Component
class ArtemisXsdListener(
    private val xmlMapper: XmlMapper
) {

    companion object: Log()

    @JmsListener(destination = $$"${queues.xml.in}")
    fun processXmlMessage(xmlMessage: String) {
        logger.infoM("\n<---- Received XML message:\n${xmlMessage.toPrettyXml()}")

        val userEvent = xmlMapper.readValue(xmlMessage, UserEvent::class.java)
        logger.infoM("Parsed XML UserEvent -> username: ${userEvent.username}, type: ${userEvent.eventType}")
    }

    @JmsListener(destination = $$"${queues.xsd.crm.in}")
    fun processCrmMessage(xmlMessage: String) {
        logger.infoM("\n<---- Received CRM message:\n${xmlMessage.toPrettyXml()}")

        val userEvent = xmlMapper.readValue(xmlMessage, UserEvent::class.java)
        logger.infoM("Parsed XML UserEvent -> username: ${userEvent.username}, type: ${userEvent.eventType}")
    }

    @JmsListener(destination = $$"${queues.xsd.billing.in}")
    fun processBillingMessage(xmlMessage: String) {
        logger.infoM("\n<---- Received BILLING message:\n${xmlMessage.toPrettyXml()}")

        val userEvent = xmlMapper.readValue(xmlMessage, UserEvent::class.java)
        logger.infoM("Parsed XML UserEvent -> username: ${userEvent.username}, type: ${userEvent.eventType}")
    }

    @JmsListener(destination = $$"${queues.xsd.dlq.in}")
    fun processDlqMessage(xmlMessage: String) {
        logger.warnM("\n<---- Received INVALID message in DLQ:\n${xmlMessage.toPrettyXml()}")

        val userEvent = xmlMapper.readValue(xmlMessage, UserEvent::class.java)
        logger.infoM("Parsed XML UserEvent -> username: ${userEvent.username}, type: ${userEvent.eventType}")
    }
}