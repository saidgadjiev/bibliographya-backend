package ru.saidgadjiev.bibliography.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by said on 25.03.2018.
 */
public final class ResponseUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ResponseUtils() { }

    public static void sendResponseMessage(HttpServletResponse response, int status, Object object) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter writer = response.getWriter();

        writer.write(OBJECT_MAPPER.writeValueAsString(object));
        writer.flush();
        writer.close();
    }

    public static void sendResponseMessage(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
    }
}
