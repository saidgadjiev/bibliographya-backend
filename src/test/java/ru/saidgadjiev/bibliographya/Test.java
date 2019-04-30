package ru.saidgadjiev.bibliographya;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.validator.routines.EmailValidator;
import org.junit.jupiter.api.Assertions;

/**
 * Created by said on 25/04/2019.
 */
public class Test {

    @org.junit.Test
    public void test() throws NumberParseException {
        String email = "said@mail.ru";

        Assertions.assertTrue(EmailValidator.getInstance().isValid(email));

        String phone = "79032691388";

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phone, null);

        System.out.println(phoneNumber.getCountryCode());
        System.out.println(phoneNumber.getNationalNumber());
    }
}
