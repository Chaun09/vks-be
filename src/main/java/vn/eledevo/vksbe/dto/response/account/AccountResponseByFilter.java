package vn.eledevo.vksbe.dto.response.account;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponseByFilter {
    Long id;
    String username;
    String fullName;
    String roleName;
    Boolean isCreateCase;
    String departmentName;
    String organizationName;
    String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate updatedAt;

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
