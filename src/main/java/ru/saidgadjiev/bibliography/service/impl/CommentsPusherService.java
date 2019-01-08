package ru.saidgadjiev.bibliography.service.impl;

import com.google.firebase.database.DatabaseReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.model.firebase.FirebaseComment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 04.01.2019.
 */
@Service
public class CommentsPusherService {

    private final DatabaseReference databaseReference;

    @Autowired
    public CommentsPusherService(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public void addComment(BiographyComment comment) {
        databaseReference
                .child("comments")
                .child(String.valueOf(comment.getId()))
                .setValue(
                        new FirebaseComment(comment.getUserId(), comment.getBiographyId()),
                        (databaseError, newDatabaseReference) -> {
                            databaseReference.child("biographies/" + comment.getBiographyId() + "/comments")
                                    .child(String.valueOf(comment.getId()))
                                    .setValueAsync(true);
                        }
                );
    }
}
