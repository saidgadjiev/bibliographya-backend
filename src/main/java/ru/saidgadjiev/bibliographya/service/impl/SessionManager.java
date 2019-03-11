package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class SessionManager {

    private MessageSource messageSource;

    private SecurityService securityService;

    @Autowired
    public SessionManager(MessageSource messageSource, SecurityService securityService) {
        this.messageSource = messageSource;
        this.securityService = securityService;
    }

    public void setRestorePassword(HttpServletRequest request, User user) {
        setState(request, SessionState.RESTORE_PASSWORD, user, new HashMap<String, Object>() {{
            put("email", user.getEmail());
        }});
    }

    public void setChangeEmail(HttpServletRequest request, String email, User user) {
        setState(request, SessionState.CHANGE_EMAIL, user, new HashMap<String, Object>() {{
            put("email", email);
        }});
    }

    public void setSignUp(HttpServletRequest request, SignUpRequest signUpRequest) {
        HttpSession session = request.getSession(true);

        session.setAttribute("state", SessionState.SIGN_UP_CONFIRM);
        session.setAttribute("firstName", signUpRequest.getFirstName());
        session.setAttribute("lastName", signUpRequest.getLastName());
        session.setAttribute("middleName", signUpRequest.getMiddleName());
    }

    public SignUpRequest getSignUp(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || !Objects.equals(session.getAttribute("state"), SessionState.SIGN_UP_CONFIRM)) {
            return null;
        }
        SignUpRequest signUpRequest = new SignUpRequest();

        signUpRequest.setFirstName((String) session.getAttribute("firstName"));
        signUpRequest.setLastName((String) session.getAttribute("lastName"));
        signUpRequest.setMiddleName((String) session.getAttribute("middleName"));

        return signUpRequest;
    }

    public SessionState getState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return SessionState.NONE;
        }

        SessionState sessionState = (SessionState) session.getAttribute("state");

        return sessionState == null ? SessionState.NONE : sessionState;
    }

    public void setCode(HttpServletRequest request, int code, long expiredAt) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.setAttribute("code", code);
        session.setAttribute("expiredAt", expiredAt);
    }

    public Integer getCode(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Integer) session.getAttribute("code");
    }

    public String getEmail(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (String) session.getAttribute("email");
    }

    public Long getExpiredAt(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Long) session.getAttribute("expiredAt");
    }

    public void removeState(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            for (Enumeration<String> attrNames = session.getAttributeNames(); attrNames.hasMoreElements();) {
                session.removeAttribute(attrNames.nextElement());
            }
        }
    }

    public String getEmailSubject(HttpServletRequest request, Locale locale) {
        SessionState sessionState = getState(request);

        if (sessionState == null) {
            return null;
        }
        switch (sessionState) {
            case RESTORE_PASSWORD:
                return messageSource.getMessage("confirm.restorePassword.subject", new Object[] {}, locale);
            case CHANGE_EMAIL:
                return messageSource.getMessage("confirm.changeEmail.subject", new Object[] {}, locale);
            case SIGN_UP_CONFIRM:
                return messageSource.getMessage("confirm.signUp.subject", new Object[] {}, locale);
            case NONE:
                break;
        }

        return null;
    }

    public String getEmailMessage(HttpServletRequest request, Locale locale) {
        SessionState sessionState = getState(request);

        if (sessionState == null) {
            return null;
        }
        String code = String.valueOf(getCode(request));

        switch (sessionState) {
            case RESTORE_PASSWORD: {
                String firstName = (String) request.getSession(false).getAttribute("firstName");

                return messageSource.getMessage(
                        "confirm.restorePassword.message",
                        new Object[]{firstName, code},
                        locale
                );
            }
            case CHANGE_EMAIL: {
                User user = (User) securityService.findLoggedInUser();

                return messageSource.getMessage(
                        "confirm.changeEmail.message",
                        new Object[]{user.getBiography().getFirstName(), code},
                        locale
                );
            }
            case SIGN_UP_CONFIRM: {
                SignUpRequest signUpRequest = getSignUp(request);

                return messageSource.getMessage(
                        "confirm.signUp.message",
                        new Object[]{signUpRequest.getFirstName(), code},
                        locale
                );
            }
            case NONE:
                break;
        }

        return null;
    }

    private void setState(HttpServletRequest request, SessionState state, User user, Map<String, Object> args) {
        HttpSession session = request.getSession(true);

        session.setAttribute("state", state);

        if (user != null) {
            session.setAttribute("firstName", user.getBiography().getFirstName());
        }

        if (args != null) {
            args.forEach(session::setAttribute);
        }
    }
}
