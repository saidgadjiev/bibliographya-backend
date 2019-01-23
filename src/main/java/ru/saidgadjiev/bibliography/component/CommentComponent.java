package ru.saidgadjiev.bibliography.component;

import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliography.service.impl.BiographyCommentService;

/**
 * Created by said on 23.01.2019.
 */
@Component("comment")
public class CommentComponent {

    private final BiographyCommentService commentService;

    public CommentComponent(BiographyCommentService commentService) {
        this.commentService = commentService;
    }

    public boolean isIAuthor(int commentId) {
        return commentService.isIAuthor(commentId);
    }
}
