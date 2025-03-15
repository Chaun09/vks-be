package vn.eledevo.vksbe.dto.response;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.http.ResponseEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    String code;
    String message;
    T result;

    public ApiResponse() {
        this.code = String.valueOf(OK.value());
        this.message = OK.getReasonPhrase();
    }

    public ApiResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResponse(String code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> ApiResponse<T> ok(T body) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setResult(body);
        return response;
    }

    public static <T> ResponseEntity<ApiResponse<T>> okEntity(T body) {
        return ResponseEntity.ok(ok(body));
    }
}
