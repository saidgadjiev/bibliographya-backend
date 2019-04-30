package ru.saidgadjiev.bibliographya.dao.api;

import ru.saidgadjiev.bibliographya.domain.Verification;

public interface VerificationDao {

    void create(Verification verification);

    Verification get(String verificationKey, String code);

    void remove(Verification verification);
}
