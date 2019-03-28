package ru.saidgadjiev.bibliographya.html.truncate;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by said on 23/03/2019.
 */
public class HtmlTruncate {

    private static final ScriptEngine ENGINE = new ScriptEngineManager().getEngineByName("nashorn");

    static {
        try {
            ENGINE.eval(Script.SCRIPT);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public static String truncate(String source, int maxLength) throws ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) ENGINE;

        return (String) invocable.invokeFunction("truncate", source, maxLength);
    }
}
