package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.FeedbackDao;
import ru.saidgadjiev.bibliographya.domain.Feedback;
import ru.saidgadjiev.bibliographya.model.FeedbackRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import java.util.List;

@Service
public class FeedbackService {

    private FeedbackDao feedbackDao;

    public FeedbackService(FeedbackDao feedbackDao) {
        this.feedbackDao = feedbackDao;
    }

    public void create(FeedbackRequest feedbackRequest) {
        Feedback feedback = new Feedback();

        feedback.setContent(feedbackRequest.getContent());

        feedbackDao.create(feedback);
    }

    public Page<Feedback> getList(OffsetLimitPageRequest pageRequest) {
        List<Feedback> feedbackList = feedbackDao.getList(pageRequest.getPageSize(), pageRequest.getOffset());

        return new PageImpl<>(feedbackList, pageRequest, feedbackList.size());
    }
}
