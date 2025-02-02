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
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.exception.handler.PhoneOrEmailIsInvalidException;

/**
 * Created by said on 25/04/2019.
 */
public class AuthKeyArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthKey.class);
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

    public static AuthKey resolve(String verificationKeyParameter) {
        AuthKey authKey = new AuthKey();

        EmailValidator emailValidator = EmailValidator.getInstance();

        boolean isValidEmail = emailValidator.isValid(verificationKeyParameter);

        if (isValidEmail) {
            authKey.setEmail(verificationKeyParameter);
            authKey.setType(AuthKey.Type.EMAIL);

            return authKey;
        }

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            if (!verificationKeyParameter.startsWith("+")) {
                verificationKeyParameter = "+" + verificationKeyParameter;
            }

            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(verificationKeyParameter, null);

            authKey.setCountryCode(String.valueOf(phoneNumber.getCountryCode()));
            authKey.setPhone(String.valueOf(phoneNumber.getNationalNumber()));
            authKey.setType(AuthKey.Type.PHONE);

            return authKey;
        } catch (NumberParseException e) {
            throw new PhoneOrEmailIsInvalidException();
        }
    }
}
