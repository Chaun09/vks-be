package vn.eledevo.vksbe.constant.ErrorCodes;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum CaseErrorCode implements BaseErrorCode {
    CASE_NOT_FOUND(NOT_FOUND, "CAS-01", "Vụ án không tồn tại", new HashMap<>()),
    CASE_NOT_ACCESS(FORBIDDEN, "CAS-02", "Bạn không có quyền truy cập", new HashMap<>()),
    CASE_EXISTED(OK, "CAS-03", "Tên vụ án đã tồn tại", new HashMap<>()),
    CASE_CODE_EXISTED(OK, "CAS-04", "Mã vụ án đã tồn tại", new HashMap<>()),
    START_TIME_GREATER_THAN_END_TIME(
            OK, "CAS-05", "Thời gian bắt đầu không được lớn hơn thời gian kết thúc.", new HashMap<>()),
    CASE_ACCESS_DENIED(OK, "CAS-06", "Bạn không có quyền vào vụ án của phòng ban này", new HashMap<>()),
    CASE_EDIT_PERMISSION_DENIED(OK, "CAS-07", "Bạn không có quyền chỉnh sửa trong vụ án này", new HashMap<>()),
    CASE_CITIZEN_NOT_FOUND_IN_LIST(
            BAD_REQUEST, "CAS-08", "Danh sách bạn gửi xuống có công dân không tồn tại", new HashMap<>()),
    CASE_ACCOUNT_NOT_DEPARTMENT(OK, "CAS-09", "Bạn không thuộc phòng ban trong vụ án này", new HashMap<>()),
    CASE_NOT_PERMISSION_EDIT(
            OK, "CAS-10", "Bạn không có quyền chỉnh sửa danh sách kiểm sát viên trong vụ án này", new HashMap<>()),
    ACCOUNT_NOT_FOUND_IN_LIST(
            BAD_REQUEST, "CAS-11", "Trong danh sách có tài khoản ko tồn tại trong phòng ban", new HashMap<>()),
    CASE_CREATE_PERMISSION_DENIED(
            OK,
            "CAS-12",
            "Bạn không được phép tạo vụ án trong phòng ban lãnh đạo và phòng ban kỹ thuật",
            new HashMap<>()),
    NOT_REMOVE_PERMISSION_DOWNLOAD_CASE(
            OK, "CAS-13", "Bạn không có được phép gỡ quyền tải vụ án của tài khoản {0}", new HashMap<>()),
    ;

    private final HttpStatusCode statusCode;
    private final String code; // Đảm bảo `code` là String
    private final String message;
    private final Map<String, Optional<?>> result;

    CaseErrorCode(HttpStatusCode statusCode, String code, String message, Map<String, Optional<?>> result) {
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
