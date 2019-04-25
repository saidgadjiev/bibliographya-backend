package ru.saidgadjiev.bibliographya;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Created by said on 25/04/2019.
 */
public class Main {

    public static void main(String[] args) throws NumberParseException {
        String email = "said@mail.ru";

        boolean validEmail = EmailValidator.getInstance().isValid(email);

        String phone = "+41446681800";

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phone, null);

        System.out.println(phoneNumber.getCountryCode());
        System.out.println(phoneNumber.getNationalNumber());
    }
}
