package ru.saidgadjiev.bibliography;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.saidgadjiev.bibliography.properties.JwtProperties;

@EnableConfigurationProperties(value = {
		JwtProperties.class
})
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
