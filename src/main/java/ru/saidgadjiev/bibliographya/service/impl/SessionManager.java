package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Objects;

@Service
public class SessionManager {

    private MessageSource messageSource;

    private SecurityService securityService;

    @Autowired
    public SessionManager(MessageSource messageSource, SecurityService securityService) {
        this.messageSource = messageSource;
        this.securityService = securityService;
    }

    public void setSignUp(HttpServletRequest request, SignUpRequest signUpRequest) {
        HttpSession session = request.getSession(true);

        session.setAttribute("state", SessionState.SIGN_UP_CONFIRM);
        session.setAttribute("email", signUpRequest.getEmail());
        session.setAttribute("firstName", signUpRequest.getFirstName());
        session.setAttribute("lastName", signUpRequest.getLastName());
        session.setAttribute("middleName", signUpRequest.getMiddleName());
        session.setAttribute("password", signUpRequest.getPassword());
    }

    public SignUpRequest getSignUp(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || Objects.equals(session.getAttribute("state"), SessionState.SIGN_UP_CONFIRM)) {
            return null;
        }
        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setEmail((String) session.getAttribute("email"));
        signUpRequest.setFirstName((String) session.getAttribute("firstName"));
        signUpRequest.setLastName((String) session.getAttribute("lastName"));
        signUpRequest.setMiddleName((String) session.getAttribute("middleName"));
        signUpRequest.setPassword((String) session.getAttribute("password"));

        return signUpRequest;
    }

    public void removeSignUp(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.removeAttribute("state");
            session.removeAttribute("email");
            session.removeAttribute("firstName");
            session.removeAttribute("lastName");
            session.removeAttribute("middleName");
            session.removeAttribute("password");
        }
    }

    public SessionState getState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return SessionState.NONE;
        }

        return (SessionState) session.getAttribute("state");
    }

    public void setCode(HttpServletRequest request, int code, long expiredAt) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.setAttribute("code", code);
        session.setAttribute("expiredAt", expiredAt);
    }

    public void removeCode(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute("code");
        session.removeAttribute("expiredAt");
    }

    public Integer getCode(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Integer) session.getAttribute("code");
    }

    public Long getExpiredAt(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Long) session.getAttribute("expiredAt");
    }

    public String getEmailSubject(HttpServletRequest request) {
        SessionState sessionState = getState(request);

        switch (sessionState) {
            case CHANGE_EMAIL:
                return messageSource.getMessage("confirm.changeEmail.subject", new Object[] {}, Locale.getDefault());
            case SIGN_UP_CONFIRM:
                return messageSource.getMessage("confirm.signUp.subject", new Object[] {}, Locale.getDefault());
        }

        return null;
    }

    public String getEmailMessage(HttpServletRequest request) {
        SessionState sessionState = getState(request);
        Integer code = getCode(request);

        switch (sessionState) {
            case CHANGE_EMAIL:
                User user = (User) securityService.findLoggedInUser();

                return messageSource.getMessage(
                        "confirm.changeEmail.message",
                        new Object[]{ user.getBiography().getFirstName(), code },
                        Locale.getDefault()
                );
            case SIGN_UP_CONFIRM:
                SignUpRequest signUpRequest = getSignUp(request);

                return messageSource.getMessage(
                        "confirm.signUp.message",
                        new Object[]{ signUpRequest.getFirstName(), code },
                        Locale.getDefault()
                );
        }

        return null;
    }
}
