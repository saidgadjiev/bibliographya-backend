package ru.saidgadjiev.bibliography.dao.impl.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.dao.api.BiographyDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.model.firebase.BiographyStats;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Created by said on 08.01.2019.
 */
@Repository
@Qualifier("firebase")
public class FirebaseBiographyDao implements BiographyDao {

    private final FirebaseDatabase firebaseDatabase;

    public FirebaseBiographyDao(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    @Override
    public Biography save(Biography biography) throws SQLException {
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        databaseReference
                .child("stats/biography/biography" + biography.getId())
                .setValueAsync(new HashMap<String, Object>() {{
                    put("likesCount", 0);
                    put("commentsCount", 0);
                }});

        return null;
    }

    @Override
    public Biography getBiography(Collection<FilterCriteria> biographyCriteria) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Biography> getBiographiesList(int limit,
                                              long offset,
                                              String categoryName,
                                              Collection<FilterCriteria> biographyCriteria,
                                              Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countOff() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Biography getById(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BiographyUpdateStatus update(Biography biography) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public BiographyStats getBiographyStats(Integer biographyId) {
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        CompletableFuture<BiographyStats> completableFuture = new CompletableFuture<>();

        databaseReference
                .child("stats/biography/biography" + biographyId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        completableFuture.complete(dataSnapshot.getValue(BiographyStats.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        try {
            return completableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Integer, BiographyStats> getBiographiesStats(List<Integer> biographiesIds) {
        if (biographiesIds.isEmpty()) {
            return Collections.emptyMap();
        }
        if (biographiesIds.size() == 1) {
            Map<Integer, BiographyStats> result = new HashMap<>();

            result.put(biographiesIds.iterator().next(), getBiographyStats(biographiesIds.iterator().next()));

            return result;
        }
        Collections.sort(biographiesIds);
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        CompletableFuture<Map<Integer, BiographyStats>> completableFuture = new CompletableFuture<>();

        databaseReference
                .child("stats/biography")
                .orderByKey()
                .startAt("biography" + biographiesIds.get(0))
                .endAt("biography" + biographiesIds.get(biographiesIds.size() - 1))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<Integer, BiographyStats> stats = new HashMap<>();

                        dataSnapshot.getChildren().forEach(new Consumer<DataSnapshot>() {
                            @Override
                            public void accept(DataSnapshot snapshot) {
                                BiographyStats biographyStats = snapshot.getValue(BiographyStats.class);

                                stats.put(biographyStats.getId(), biographyStats);
                            }
                        });

                        completableFuture.complete(stats);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        try {
            return completableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
