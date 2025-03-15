package vn.eledevo.vksbe.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public class ValidationException extends Exception {

    private final Map<String, String> errors;

    public ValidationException(String key, String value) {
        this.errors = new HashMap<>();
        this.errors.put(key, value);
    }

    public ValidationException(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }
}
