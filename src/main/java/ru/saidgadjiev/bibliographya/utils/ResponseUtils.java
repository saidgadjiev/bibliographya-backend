package ru.saidgadjiev.bibliographya.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by said on 25.03.2018.
 */
public final class ResponseUtils {

    private ResponseUtils() { }

    public static void sendResponseMessage(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
    }

    public static void sendResponseMessage(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter writer = response.getWriter();

        writer.write(message);
        writer.flush();
        writer.close();
    }
}
