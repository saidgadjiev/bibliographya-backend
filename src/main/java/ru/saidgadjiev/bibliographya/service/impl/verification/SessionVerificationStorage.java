package ru.saidgadjiev.bibliographya.service.impl.verification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
@Qualifier("session")
public class SessionVerificationStorage implements VerificationStorage {

    @Override
    public void removeAttr(HttpServletRequest request, String attr) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return session.getAttribute(attr);
    }

    @Override
    public void setAttr(HttpServletRequest request, String attr, Object data) {
        HttpSession session = request.getSession(true);

        session.setAttribute(attr, data);
    }
}
