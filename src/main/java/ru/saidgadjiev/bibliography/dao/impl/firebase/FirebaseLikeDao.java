package ru.saidgadjiev.bibliography.dao.impl.firebase;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.dao.api.BiographyLikeDao;
import ru.saidgadjiev.bibliography.domain.BiographyLike;

import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 08.01.2019.
 */
@Repository
@Qualifier("firebase")
public class FirebaseLikeDao implements BiographyLikeDao {

    private final DatabaseReference databaseReference;

    @Autowired
    public FirebaseLikeDao(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    @Override
    public int create(BiographyLike like) {
        databaseReference
                .child("stats/biography/biography" + like.getBiographyId() + "/likesCount")
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        long likesCount = 1;

                        if (mutableData.getValue() != null) {
                            likesCount = mutableData.getValue(Long.class);

                            likesCount += 1;
                        }

                        mutableData.setValue(likesCount);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });

        return 0;
    }

    @Override
    public int delete(BiographyLike like) {
        databaseReference
                .child("stats/biography/biography" + like.getBiographyId() + "/likesCount")
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        long likesCount = 0;

                        if (mutableData.getValue() != null) {
                            likesCount = mutableData.getValue(Long.class);

                            if (likesCount > 0) {
                                likesCount -= 1;
                            }
                        }

                        mutableData.setValue(likesCount);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });

        return 0;
    }

    @Override
    public int getLikesCount(int biographyId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLiked(int userId, int biographyId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, Boolean> isLikedByBiographies(int userId, Collection<Integer> biographiesIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, Integer> getLikesCountByBiographies(Collection<Integer> biographiesIds) {
        throw new UnsupportedOperationException();
    }
}
