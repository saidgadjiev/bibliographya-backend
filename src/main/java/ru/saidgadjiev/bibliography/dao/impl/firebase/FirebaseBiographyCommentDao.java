package ru.saidgadjiev.bibliography.dao.impl.firebase;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.dao.api.BiographyCommentDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by said on 07.01.2019.
 */
//@Repository
@Qualifier("firebase")
public class FirebaseBiographyCommentDao implements BiographyCommentDao {

    private final FirebaseDatabase firebaseDatabase;

    @Autowired
    public FirebaseBiographyCommentDao(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    @Override
    public BiographyComment create(BiographyComment biographyComment) {
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        databaseReference
                .child("stats/biography/biography" + biographyComment.getBiographyId() + "/commentsCount")
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        long commentsCount = 1;

                        if (mutableData.getValue() != null) {
                            commentsCount = mutableData.getValue(Long.class);

                            commentsCount += 1;
                        }

                        mutableData.setValue(commentsCount);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
        /*Map<String, Object> commentMap = new HashMap<>();

        commentMap.put("id", biographyComment.getId());
        commentMap.put("content", biographyComment.getContent());
        commentMap.put("createdAt", biographyComment.getCreatedAt().getTime());
        commentMap.put("biographyId", biographyComment.getBiographyId());
        commentMap.put("userId", biographyComment.getUserId());
        commentMap.put("parentId", biographyComment.getParentId());
        commentMap.put("parentBiographyId", biographyComment.getParent().getBiographyId());

        databaseReference
                .child("biography_comment")
                .child("comment" + biographyComment.getId())
                .setValue(commentMap, (databaseError, newDatabaseReference) -> databaseReference
                        .child("biography")
                        .child(String.valueOf(biographyComment.getBiographyId()))
                        .child("comments")
                        .child("biography" + biographyComment.getId())
                        .setValueAsync(true));*/

        return null;
    }

    @Override
    public int delete(int biographyId, int commentId) {
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        /*databaseReference
                .child("biography_comment")
                .child(String.valueOf(commentId))
                .removeValue((databaseError, newDatabaseReference) -> databaseReference
                        .child("biography")
                        .child(String.valueOf(biographyId))
                        .child("comments")
                        .removeValueAsync());*/
        databaseReference
                .child("stats/biography/biography" + biographyId + "/commentsCount")
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        long commentsCount = 0;

                        if (mutableData.getValue() != null) {
                            commentsCount = mutableData.getValue(Long.class);

                            if (commentsCount > 0) {
                                commentsCount -= 1;
                            }
                        }

                        mutableData.setValue(commentsCount);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });

        return 1;
    }

    @Override
    public List<BiographyComment> getComments(int biographyId, Sort sort, int limit, long offset, Integer afterKey) {
        /*Query query = databaseReference
                .child("biography/biography" + biographyId + "/comments")
                .orderByKey()
                .limitToFirst(limit);

        if (afterKey != null) {
            query.startAt("comment" + afterKey);
        }

        CompletableFuture<List<BiographyComment>> completableFuture = CompletableFuture.supplyAsync(() -> {
            CompletableFuture<List<String>> future = new CompletableFuture<>();

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> keys = new ArrayList<>();

                    dataSnapshot.getChildren().forEach(snapshot -> keys.add(snapshot.getKey()));

                    future.complete(keys);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            try {
                return future.get();
            } catch (Exception ex) {
                throw  new RuntimeException(ex);
            }
        }).thenApplyAsync(strings -> {
            CompletableFuture<List<BiographyComment>> future = new CompletableFuture<>();

            databaseReference
                    .child("biography_comment")
                    .orderByKey()
                    .startAt(strings.get(0))
                    .endAt(strings.get(strings.size() - 1))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<BiographyComment> comments = new ArrayList<>();

                            dataSnapshot.getChildren().forEach(new Consumer<DataSnapshot>() {
                                @Override
                                public void accept(DataSnapshot snapshot) {
                                    Map<String, Object> values = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {});

                                    comments.add(map(values));
                                }
                            });

                            future.complete(comments);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            try {
                return future.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).thenApplyAsync(biographyComments -> {
            Map<Integer, Biography> biographyMap = new HashMap<>();

            try {
                for (BiographyComment comment : biographyComments) {
                    if (!biographyMap.containsKey(comment.getBiographyId())) {
                        biographyMap.put(comment.getBiographyId(), getBiography(comment.getBiographyId()).get());
                    }

                    comment.setBiography(biographyMap.get(comment.getBiographyId()));

                    if (comment.getParentId() != null) {
                        int parentBiographyId = comment.getParent().getBiographyId();

                        if (!biographyMap.containsKey(parentBiographyId)) {
                            biographyMap.put(parentBiographyId, getBiography(parentBiographyId).get());
                        }

                        comment.getParent().setBiography(biographyMap.get(parentBiographyId));
                    }

                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            return biographyComments;
        });

        try {
            return completableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

        throw new UnsupportedOperationException();
    }

    @Override
    public long countOffByBiographyId(int biographyId) {
        /*//TODO: поменять подсчет через Cloud function
        CompletableFuture<Long> completableFuture = new CompletableFuture<>();

        databaseReference
                .child("biography/biography" + biographyId + "/comments")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        completableFuture.complete(dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        try {
            return completableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, Long> countOffByBiographiesIds(Collection<Integer> biographiesIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BiographyComment getById(int id) {
        /*CompletableFuture<BiographyComment> completableFuture = CompletableFuture.supplyAsync(() -> {
            CompletableFuture<BiographyComment> future = new CompletableFuture<>();

            databaseReference
                    .child("biography_comment/comment" + id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> values = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {});

                            future.complete(map(values));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            try {
                return future.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).thenApplyAsync(comment -> {
            try {
                comment.setBiography(getBiography(comment.getBiographyId()).get());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            return comment;
        }).thenApplyAsync(comment -> {
            if (comment.getParentId() == null) {
                return comment;
            }

            try {
                comment.getParent().setBiography(getBiography(comment.getParent().getBiographyId()).get());

                return comment;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });


        try {
            return completableFuture.get();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }*/

        throw new UnsupportedOperationException();
    }

    @Override
    public int updateContent(Integer commentId, String content) {
        /*databaseReference
                .child("biography_comment/comment" + commentId)
                .updateChildrenAsync(new HashMap<String, Object>() {{
                    put("content", content);
                }});

        return 1;*/

        throw new UnsupportedOperationException();
    }

    private CompletableFuture<Biography> getBiography(int biographyId) {
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        CompletableFuture<Biography> completableFuture = new CompletableFuture<>();

        databaseReference
                .child("biography/biography" + biographyId + "/body")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Biography biography = new Biography();

                        Map<String, Object> values = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {});

                        biography.setId(biographyId);
                        biography.setFirstName((String) values.get("firstName"));
                        biography.setLastName((String) values.get("lastName"));

                        completableFuture.complete(biography);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("NO");
                    }
                });

        return completableFuture;
    }

    private BiographyComment map(Map<String, Object> values) {
        BiographyComment biographyComment = new BiographyComment();

        biographyComment.setId(((Long) values.get("id")).intValue());
        biographyComment.setContent((String) values.get("content"));
        biographyComment.setCreatedAt(new Timestamp((Long) values.get("createdAt")));
        biographyComment.setBiographyId(((Long) values.get("biographyId")).intValue());
        biographyComment.setUserId(((Long) values.get("userId")).intValue());

        Long parentId = (Long) values.get("parentId");

        if (parentId != null) {
            biographyComment.setParentId(parentId.intValue());

            Long parentBiographyId = (Long) values.get("parentBiographyId");

            BiographyComment parent = new BiographyComment();

            parent.setId(parentId.intValue());
            parent.setBiographyId(parentBiographyId.intValue());

            biographyComment.setParent(parent);
        }

        return biographyComment;
    }
}
