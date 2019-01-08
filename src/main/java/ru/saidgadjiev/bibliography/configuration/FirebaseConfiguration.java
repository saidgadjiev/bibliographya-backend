package ru.saidgadjiev.bibliography.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import ru.saidgadjiev.bibliography.properties.FirebaseProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by said on 06.01.2019.
 */
@Configuration
public class FirebaseConfiguration {

    private final FirebaseProperties firebaseProperties;

    @Autowired
    public FirebaseConfiguration(FirebaseProperties firebaseProperties) {
        this.firebaseProperties = firebaseProperties;
    }

    @Bean
    public DatabaseReference databaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @PostConstruct
    public void init() throws IOException {
        Resource resource = new ClassPathResource(firebaseProperties.getConfigPath());

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .setDatabaseUrl(firebaseProperties.getDatabaseUrl())
                .build();

        FirebaseApp.initializeApp(options);
    }
}
