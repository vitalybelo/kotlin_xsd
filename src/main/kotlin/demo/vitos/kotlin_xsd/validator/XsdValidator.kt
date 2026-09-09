package demo.vitos.kotlin_xsd.validator

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

@Component
class XsdValidator {

    private val schema = SchemaFactory
        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .newSchema(ClassPathResource("xsd/user-event.xsd").url)

    /**
     * Если XML не соответствует XSD, этот метод выбросит Exception
     */
    fun validate(xmlPayload: String) {
        val validator = schema.newValidator()
        validator.validate(StreamSource(StringReader(xmlPayload)))
    }
}