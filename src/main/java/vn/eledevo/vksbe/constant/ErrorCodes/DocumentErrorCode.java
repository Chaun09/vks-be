package vn.eledevo.vksbe.constant.ErrorCodes;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum DocumentErrorCode implements BaseErrorCode {
    PARENT_DOC_NOT_FOUND(OK, "DOC-05", "Folder cha không tồn tại", new HashMap<>()),
    DOC_NOT_FOUND(OK, "DOC-02", "Tài liệu này không tồn tại", new HashMap<>()),
    E_UNSUPPORT_TYPE_FILE(BAD_REQUEST, "DOC-03", "Không hỗ trợ định dạng loại file này!", new HashMap<>()),
    CAN_NOT_MOVE(OK, "DOC-04", "Không thể di chuyển được file !", new HashMap<>()),
    DOCUMENT_NOT_FOUND(OK, "DOC-01", "Tài liệu không tồn tại", new HashMap<>()),
    CAN_NOT_MOVE_FILE(OK, "DOC-06", "Không thể di chuyển tài liệu đến chính nó ", new HashMap<>()),
    CAN_NOT_UPDATE_DEFAULT(BAD_REQUEST, "400", "Không thể cập nhập tài liệu gốc ", new HashMap<>()),
    CAN_NOT_MOVE_TRASH(OK, "DOC-05", "Không thể di chuyển tài liệu đến thư mục thùng rác ", new HashMap<>()),
    CAN_NOT_DELETE_DEFAULT(BAD_REQUEST, "DOC-06", "Không thể xóa tài liệu gốc ", new HashMap<>()),
    FILE_CAN_NOT_EMPTY(BAD_REQUEST, "DOC-07", "File không được rỗng", new HashMap<>()),
    ;

    private final HttpStatusCode statusCode;
    private final String code; // Đảm bảo `code` là String
    private final String message;
    private final Map<String, Optional<?>> result;

    DocumentErrorCode(HttpStatusCode statusCode, String code, String message, Map<String, Optional<?>> result) {
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
