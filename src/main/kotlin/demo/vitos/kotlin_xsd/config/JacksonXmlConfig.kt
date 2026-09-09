package demo.vitos.kotlin_xsd.config

import tools.jackson.dataformat.xml.XmlMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonXmlConfig {

    @Bean
    fun xmlMapper(): XmlMapper {
        return XmlMapper()
    }
}