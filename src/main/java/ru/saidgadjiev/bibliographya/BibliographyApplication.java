package ru.saidgadjiev.bibliographya;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.saidgadjiev.bibliographya.properties.FacebookProperties;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.properties.VKProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

@EnableConfigurationProperties(value = {
        JwtProperties.class,
		FacebookProperties.class,
		VKProperties.class,
        StorageProperties.class
})
@EnableScheduling
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
	public void setStorageService(@Qualifier("fileSystemStorageService") StorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	public void run(String... strings) throws Exception {
		storageService.init();
	}
}
