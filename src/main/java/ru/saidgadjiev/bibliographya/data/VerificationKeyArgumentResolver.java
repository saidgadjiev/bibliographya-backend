package ru.saidgadjiev.bibliographya.data;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.saidgadjiev.bibliographya.domain.VerificationKey;

/**
 * Created by said on 25/04/2019.
 */
public class VerificationKeyArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(VerificationKey.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        String verificationKeyParameter = webRequest.getParameter("verificationKey");

        if (StringUtils.isBlank(verificationKeyParameter)) {
            throw new IllegalArgumentException("Verification key can't be null!");
        }

        VerificationKey verificationKey = new VerificationKey();

        EmailValidator emailValidator = EmailValidator.getInstance();

        boolean isValidEmail = emailValidator.isValid(verificationKeyParameter);

        if (isValidEmail) {
            verificationKey.setEmail(verificationKeyParameter);
            verificationKey.setType(VerificationKey.Type.EMAIL);

            return verificationKey;
        }

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(verificationKeyParameter, null);

            verificationKey.setCountryCode(String.valueOf(phoneNumber.getCountryCode()));
            verificationKey.setPhone(String.valueOf(phoneNumber.getNationalNumber()));
            verificationKey.setType(VerificationKey.Type.PHONE);

            return verificationKey;
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Phone is invalid!");
        }
    }
}
