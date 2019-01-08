package ru.saidgadjiev.bibliography;

import com.google.firebase.database.*;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.saidgadjiev.bibliography.dao.impl.firebase.FirebaseBiographyCommentDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.model.firebase.FirebaseBiography;
import ru.saidgadjiev.bibliography.properties.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@EnableConfigurationProperties(value = {
        JwtProperties.class,
		FacebookProperties.class,
		VKProperties.class,
        StorageProperties.class,
		PusherProperties.class,
		FirebaseProperties.class
})
@EnableScheduling
@SpringBootApplication
public class BibliographyApplication implements CommandLineRunner, ApplicationContextAware {

	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		try {
			SpringApplication.run(BibliographyApplication.class, args);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void run(String... args) throws Exception {
		FirebaseBiographyCommentDao bean = applicationContext.getBean(FirebaseBiographyCommentDao.class);

		List<BiographyComment> comment = bean.getComments(13, null, 1, 0, 2);

		System.out.println("YES");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
