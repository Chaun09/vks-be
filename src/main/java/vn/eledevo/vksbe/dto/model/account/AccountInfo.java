package vn.eledevo.vksbe.dto.model.account;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountInfo {
    String roleCode;
    Long departmentId;
    Boolean isConnectComputer;
    Boolean isConnectUsb;

    public AccountInfo(String roleCode, Long departmentId, Boolean isConnectComputer, Boolean isConnectUsb) {
        this.roleCode = roleCode;
        this.departmentId = departmentId;
        this.isConnectComputer = isConnectComputer;
        this.isConnectUsb = isConnectUsb;
    }
}
