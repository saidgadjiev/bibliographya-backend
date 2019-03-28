package ru.saidgadjiev.bibliographya.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by said on 23/03/2019.
 */
public class NumberUtils {

    public static int extractInt(String str) {
        Matcher matcher = Pattern.compile("\\d+").matcher(str);

        if (!matcher.find())
            throw new NumberFormatException("For input string [" + str + "]");

        return Integer.parseInt(matcher.group());
    }
}
