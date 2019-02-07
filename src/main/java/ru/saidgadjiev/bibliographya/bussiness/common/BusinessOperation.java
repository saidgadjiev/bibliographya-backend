package ru.saidgadjiev.bibliographya.bussiness.common;

import java.util.Map;

public interface BusinessOperation<T> {

    T execute(Map<String, Object> args);
}
