package my.smartcampusconnect67.library_service.config;

import jakarta.xml.ws.Endpoint;
import my.smartcampusconnect67.library_service.soap.LibraryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoapConfig {

    @Autowired
    private LibraryServiceImpl libraryService;

    @Bean
    public Endpoint libraryEndpoint() {
        // Use port 9091 to avoid conflict with Tomcat on 8084
        String url = "http://0.0.0.0:9091/ws/library";
        Endpoint endpoint = Endpoint.publish(url, libraryService);
        System.out.println("SOAP LibraryService published at " + url + "?wsdl");
        return endpoint;
    }
}
