package vn.eledevo.vksbe.dto.model.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDetailResponse {
    Long id;
    String username;
    String fullName;
    String gender;
    Long departmentId;
    String departmentName;
    Long roleId;
    String roleName;
    String roleCode;
    Long organizationId;
    String organizationName;
    String status;
    String phoneNumber;
    String avatar;
    Boolean isCreateCase;

    /** Trạng thái của nút KHÓA TÀI KHOẢN */
    Boolean isEnabledLockButton;

    Boolean isShowLockButton;

    /** Trạng thái nút SỬA */
    Boolean isEnabledEditButton;

    Boolean isShowEditButton;

    /** Trạng thái nút RESET MẬT KHẨU */
    Boolean isEnabledResetPasswordButton;

    Boolean isShowResetPasswordButton;

    /** Trạng thái nút KÍCH HOẠT TÀI KHOẢN */
    Boolean isEnabledActivateButton;

    Boolean isShowActivateButton;

    /** Trạng thái nút CẤP QUYỀN TẠO VỤ ÁN */
    Boolean isEnabledPermissionCreateCaseButton;

    Boolean isShowPermissionCreateCaseButton;

    /** Trạng thái nút GỠ QUYỀN TẠO VỤ ÁN */
    Boolean isEnabledRemovePermissionCreateCaseButton;

    Boolean isShowRemovePermissionCreateCaseButton;
}
