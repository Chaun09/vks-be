package vn.eledevo.vksbe.constant.ErrorCodes;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum CaseStatusErrorCode implements BaseErrorCode {
    CASE_STATUS_NAME_ALREADY_EXIST(OK, "CAS-S-01", "Tên trạng thái vụ án đã tồn tại", new HashMap<>()),
    CASE_STATUS_NOT_FOUND(OK, "CAS-S-02", "Trạng thái vụ án không tồn tại", new HashMap<>()),
    CASE_STATUS_DEFAULT_EMPTY(
            NOT_FOUND, "CAS-S-03", "Trạng thái vụ án mặc định chưa có trong hệ thống", new HashMap<>()),
    CASE_STATUS_IS_DEFAULT(OK, "CAS-S-04", "Trạng thái vụ án này là mặc định", new HashMap<>());
    private final HttpStatusCode statusCode;
    private final String code;
    private final String message;
    private final Map<String, Optional<?>> result;

    CaseStatusErrorCode(HttpStatusCode statusCode, String code, String message, Map<String, Optional<?>> result) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
        this.result = result;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public Map<String, Optional<?>> getResult() {
        return result;
    }

    @Override
    public void setResult(Optional<?> value) {
        // Kiểm tra nếu Optional chứa giá trị
        if (value.isPresent()) {
            Object object = value.get();
            if (object instanceof HashMap) {
                HashMap<?, ?> map = (HashMap<?, ?>) object;
                map.forEach((key, val) -> {
                    this.result.put(key.toString(), Optional.ofNullable(val));
                });
            }
        }
        if (value.isPresent()) {
            Object object = value.get();
            // Sử dụng reflection để lấy tất cả các trường (fields) của object
            Field[] fields = object.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true); // Cho phép truy cập vào các trường private

                try {
                    // Lấy tên trường (field name) làm key
                    String key = field.getName();
                    // Lấy giá trị của trường (field value) làm value và gán vào result
                    Object fieldValue = field.get(object);
                    this.result.put(key, Optional.ofNullable(fieldValue)); // Sử dụng Optional để bọc giá trị
                } catch (IllegalAccessException e) {
                    e.printStackTrace(); // Xử lý ngoại lệ nếu không thể truy cập vào trường
                }
            }
        }
    }
}
