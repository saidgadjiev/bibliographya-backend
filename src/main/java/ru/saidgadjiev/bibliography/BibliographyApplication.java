package ru.saidgadjiev.bibliography;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.saidgadjiev.bibliography.properties.FacebookProperties;
import ru.saidgadjiev.bibliography.properties.JwtProperties;
import ru.saidgadjiev.bibliography.properties.StorageProperties;
import ru.saidgadjiev.bibliography.properties.VKProperties;

@EnableConfigurationProperties(value = {
        JwtProperties.class,
		FacebookProperties.class,
		VKProperties.class,
        StorageProperties.class
		//PusherProperties.class,
		//FirebaseProperties.class
})
@EnableScheduling
@SpringBootApplication
public class BibliographyApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(BibliographyApplication.class, args);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
