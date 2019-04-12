package ru.saidgadjiev.bibliographya;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.saidgadjiev.bibliographya.properties.*;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

@EnableConfigurationProperties(value = {
        JwtProperties.class,
		FacebookProperties.class,
		VKProperties.class,
        StorageProperties.class,
		UIProperties.class,
		AppProperties.class
})
@EnableScheduling
@SpringBootApplication
public class BibliographyApplication implements CommandLineRunner {

	private StorageService categoryStorage;

	private StorageService magickStorage;

	public static void main(String[] args) {
		try {
			SpringApplication.run(BibliographyApplication.class, args);
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@Autowired
	public void setCategoryStorage(@Qualifier("category") StorageService categoryStorage) {
		this.categoryStorage = categoryStorage;
	}

	@Autowired
	public void setMagickStorage(@Qualifier("magick") StorageService magickStorage) {
		this.magickStorage = magickStorage;
	}

	@Override
	public void run(String... strings) throws Exception {
		categoryStorage.init();
		magickStorage.init();
	}
}
