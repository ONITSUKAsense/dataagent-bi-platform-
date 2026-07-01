package com.dataagent.common.model;

import lombok.Data;
import java.time.Instant;

@Data
public class R<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    private R() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> error(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> R<T> error(String message) {
        return error(500, message);
    }

    public static <T> R<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> R<T> forbidden(String message) {
        return error(403, message);
    }
}
