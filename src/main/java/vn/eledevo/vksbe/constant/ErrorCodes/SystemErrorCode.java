package vn.eledevo.vksbe.constant.ErrorCodes;

import static org.springframework.http.HttpStatus.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;

public enum SystemErrorCode implements BaseErrorCode {
    INTERNAL_SERVER(INTERNAL_SERVER_ERROR, "500", "Internal Server Error", new HashMap<>()),
    UNAUTHORIZED_SERVER(UNAUTHORIZED, "401", "Unauthorized", new HashMap<>()),
    AUTHENTICATION_SERVER(FORBIDDEN, "403", "Forbidden", new HashMap<>()),
    NOT_FOUND_SERVER(NOT_FOUND, "404", "Không tìm thấy đường dẫn API", new HashMap<>()),
    BAD_REQUEST_SERVER(BAD_REQUEST, "400", "Bad Request", new HashMap<>()),
    VALIDATE_FORM(OK, "422", "Dữ liệu không hợp lệ", new HashMap<>()),
    ORGANIZATION_STRUCTURE(
            OK, "1000", "Cơ cấu tổ chức đã thay đổi. Vui lòng đăng nhập lại để có dữ liệu mới nhất", new HashMap<>()),
    NOT_ENOUGH_PERMISSION(OK, "SYS-403", "Bạn không có quyền thao tác", new HashMap<>()),
    INVALID_PAGE_REQUEST(BAD_REQUEST, "SYS-400", "Số trang và kích thước trang phải lớn hơn 0", new HashMap<>()),
    NOT_ENOUGH_PERMISSION_CASE(OK, "CAS-D-403", "Bạn không có quyền thao tác", new HashMap<>()),
    NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE(OK, "CAS-O-403", "Bạn không có quyền thao tác", new HashMap<>()),
    ;

    private final HttpStatusCode statusCode;
    private final String code; // Đảm bảo `code` là String
    private final String message;
    private final Map<String, Optional<?>> result; // Đảm bảo `result` là Map<String, String>

    SystemErrorCode(HttpStatusCode statusCode, String code, String message, Map<String, Optional<?>> result) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
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
                map.forEach((key, val) -> this.result.put(key.toString(), Optional.ofNullable(val)));
            }
        }
        if (value.isPresent()) {
            Object object = value.get();
            // Sử dụng reflection để lấy tất cả các trường (fields) của object
            Field[] fields = object.getClass().getDeclaredFields();

            for (Field field : fields) {
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
