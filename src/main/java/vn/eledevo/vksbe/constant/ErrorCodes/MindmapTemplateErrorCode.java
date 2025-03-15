package vn.eledevo.vksbe.constant.ErrorCodes;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;

public enum MindmapTemplateErrorCode implements BaseErrorCode {
    MINDMAP_TEMPLATE_NOT_ENOUGH_PERMISSION(
            OK, "MMT-01", "Bạn không có quyền tạo sơ đồ mẫu trong phòng ban này", new HashMap<>()),
    MINDMAP_TEMPLATE_NAME_ALREADY_EXISTS(OK, "MMT-02", "Tên sơ đồ mẫu không được trùng", new HashMap<>()),
    MINDMAP_TEMPLATE_CREATE_FAILED(OK, "MMT-03", "Tạo mới sơ đồ mindmap thất bại", new HashMap<>()),
    MINDMAP_TEMPLATE_NAME_EMPTY(
            OK, "MMT-04", "Tên sơ đồ không được để trống, vui lòng lựa chọn phòng ban", new HashMap<>()),
    MINDMAP_TEMPLATE_NOT_FOUND(OK, "MMT-05", "Sơ đồ mẫu không tồn tại", new HashMap<>()),
    MINDMAP_TEMPLATE_NO_PERMISSION_TO_ACCESS(
            OK, "MMT-06", "Bạn không có quyền vào sơ đồ mẫu của phòng ban này", new HashMap<>()),
    MINDMAP_IMG_INVALID_FORMAT(BAD_REQUEST, "MMT-07", "Ảnh chỉ chấp nhận định dạng {0}", new HashMap<>()),
    MINDMAP_IMG_SIZE_EXCEEDS_LIMIT(BAD_REQUEST, "MMT-08", "Ảnh không được vượt quá {0}", new HashMap<>()),
    ;

    private final HttpStatusCode statusCode;
    private final String code; // Đảm bảo `code` là String
    private final String message;
    private final Map<String, Optional<?>> result;

    MindmapTemplateErrorCode(HttpStatusCode statusCode, String code, String message, Map<String, Optional<?>> result) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
        this.result = result;
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
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setResult(Optional<?> value) {
        if (value.isPresent()) {
            Object object = value.get();
            if (object instanceof HashMap) {
                HashMap<?, ?> map = (HashMap<?, ?>) object;
                map.forEach((key, val) -> {
                    this.result.put(key.toString(), Optional.ofNullable(val));
                });
            }
        }
    }
}
