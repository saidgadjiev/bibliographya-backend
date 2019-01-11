package ru.saidgadjiev.bibliography;

import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.saidgadjiev.bibliography.properties.*;

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
		/*FirebaseBiographyCommentDao bean = applicationContext.getBean(FirebaseBiographyCommentDao.class);

		BiographyComment comment = new BiographyComment();

		comment.setBiographyId(13);

		bean.delete(13, 1);

		System.out.println("YES");*/
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
