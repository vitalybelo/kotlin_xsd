package demo.vitos.kotlin_xsd.utils

import org.slf4j.LoggerFactory
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


private val logger = LoggerFactory.getLogger("Utils")

fun String.toPrettyXml(): String {
    return try {

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

        val stringWriter = StringWriter()
        transformer.transform(StreamSource(StringReader(this)), StreamResult(stringWriter))

        stringWriter.toString().trim()

    } catch (e: Exception) {
        logger.error("Error processing message by reason: ${e.message}, cause: ${e.cause}")
        this
    }
}
