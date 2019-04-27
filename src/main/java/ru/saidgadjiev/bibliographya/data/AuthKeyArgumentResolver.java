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
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;

/**
 * Created by said on 25/04/2019.
 */
public class AuthKeyArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthenticationKey.class);
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

        return resolve(verificationKeyParameter);
    }

    public static AuthenticationKey resolve(String verificationKeyParameter) {
        AuthenticationKey authenticationKey = new AuthenticationKey();

        EmailValidator emailValidator = EmailValidator.getInstance();

        boolean isValidEmail = emailValidator.isValid(verificationKeyParameter);

        if (isValidEmail) {
            authenticationKey.setEmail(verificationKeyParameter);
            authenticationKey.setType(AuthenticationKey.Type.EMAIL);

            return authenticationKey;
        }

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            if (!verificationKeyParameter.startsWith("+")) {
                verificationKeyParameter = "+" + verificationKeyParameter;
            }

            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(verificationKeyParameter, null);

            authenticationKey.setCountryCode(String.valueOf(phoneNumber.getCountryCode()));
            authenticationKey.setPhone(String.valueOf(phoneNumber.getNationalNumber()));
            authenticationKey.setType(AuthenticationKey.Type.PHONE);

            return authenticationKey;
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Phone is invalid!");
        }
    }
}
