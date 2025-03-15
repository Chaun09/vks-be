package vn.eledevo.vksbe.dto.model.account;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountQueryToFilter {
    Long id;
    String username;
    String fullName;
    String roleName;
    String roleCode;
    Long roleId;
    Long departmentId;
    String departmentName;
    Long organizationId;
    String organizationName;
    String status;
    Boolean isConnectComputer;
    Boolean isConnectUsb;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Boolean isCreateCase;

    /** Trạng thái nút KÍCH HOẠT TÀI KHOẢN */
    Boolean isShowLockButton;

    Boolean isEnabledLockButton;

    /** Trạng thái nút KHÓA TÀI KHOẢN */
    Boolean isShowUnlockButton;

    Boolean isEnabledUnlockButton;

    /** Trạng thái nút CẤP QUYỀN TẠO VỤ ÁN */
    Boolean isEnabledPermissionCreateCaseButton;

    Boolean isShowPermissionCreateCaseButton;

    /** Trạng thái nút GỠ QUYỀN TẠO VỤ ÁN */
    Boolean isEnabledRemovePermissionCreateCaseButton;

    Boolean isShowRemovePermissionCreateCaseButton;
}
