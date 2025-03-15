package vn.eledevo.vksbe.exception;

import lombok.Getter;
import vn.eledevo.vksbe.constant.ErrorCodes.BaseErrorCode;

@Getter
public class ApiException extends Exception {
    private final BaseErrorCode baseErrorCode;

    public ApiException(BaseErrorCode baseErrorCode) {
        super(baseErrorCode.getMessage());
        this.baseErrorCode = baseErrorCode;
    }

    // Constructor với thông điệp tùy chỉnh
    public ApiException(BaseErrorCode baseErrorCode, String message) {
        super(message);
        this.baseErrorCode = baseErrorCode;
    }

    public BaseErrorCode getCode() {
        return baseErrorCode;
    }
}
