package ru.saidgadjiev.bibliographya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.saidgadjiev.bibliographya.properties.FacebookProperties;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.properties.VKProperties;

@EnableConfigurationProperties(value = {
        JwtProperties.class,
		FacebookProperties.class,
		VKProperties.class,
        StorageProperties.class
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
