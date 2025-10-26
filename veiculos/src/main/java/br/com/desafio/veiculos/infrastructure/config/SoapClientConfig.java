package br.com.desafio.veiculos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
public class SoapClientConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Pacote onde as classes JAX-B (geradas do WSDL) estão
        // marshaller.setContextPath("br.com.f1.schemas"); 
        marshaller.setPackagesToScan("br.com.desafio.veiculos.domain.f1"); // Stub
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(marshaller);
        template.setUnmarshaller(marshaller);
        
        // Configurar Timeouts (milissegundos)
        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();
        messageSender.setConnectionTimeout(350);
        messageSender.setReadTimeout(350);

        template.setMessageSender(messageSender);
        return template;
    }
}
