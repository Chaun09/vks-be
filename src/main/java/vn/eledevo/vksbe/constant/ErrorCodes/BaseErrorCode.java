package vn.eledevo.vksbe.constant.ErrorCodes;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;

public interface BaseErrorCode {
    HttpStatusCode getStatusCode(); // Mã trạng thái HTTP tương ứng

    Map<String, Optional<?>> getResult(); // Trả về kết quả dưới dạng Object (hoặc Optional)

    String getCode(); // Mã lỗi

    String getMessage(); // Thông điệp lỗi

    void setResult(Optional<?> value);
}
