package ru.saidgadjiev.bibliography.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by said on 25.03.2018.
 */
public final class ResponseUtils {

    private ResponseUtils() { }

    public static void sendResponseMessage(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
    }
}
