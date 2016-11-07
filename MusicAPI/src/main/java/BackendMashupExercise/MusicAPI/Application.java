package BackendMashupExercise.MusicAPI;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application extends SpringBootServletInitializer
{
	public static void main(String[] args) throws Exception {
		//pretty-print json output 
		//TODO: Should not be present in production release, only dev
		System.setProperty("spring.jackson.serialization.INDENT_OUTPUT", "true");
		
		SpringApplication.run(Application.class, args);
	}
	
    @Bean
    AsyncRestTemplate asyncRestTemplate() {
        return new AsyncRestTemplate();
    }
    
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
