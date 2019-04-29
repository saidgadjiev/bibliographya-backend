package ru.saidgadjiev.bibliographya;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.saidgadjiev.bibliographya.properties.*;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

@EnableConfigurationProperties(value = {
        JwtProperties.class,
		FacebookProperties.class,
		VKProperties.class,
        StorageProperties.class,
		UIProperties.class,
		AppProperties.class,
		VerificationProperties.class
})
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class BibliographyApplication implements CommandLineRunner {

	private StorageService storageService;

	public static void main(String[] args) {
		try {
			SpringApplication.run(BibliographyApplication.class, args);
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@Autowired
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	public void run(String... strings) throws Exception {
		storageService.init();
	}
}
