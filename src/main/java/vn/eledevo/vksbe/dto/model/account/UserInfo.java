package vn.eledevo.vksbe.dto.model.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfo {
    Long id;
    String username;
    String avatar;
    String fullName;
    String gender;
    String phoneNumber;
    String roleCode;
    String departmentCode;
    Long organizationId;
    String organizationName;
    Boolean isCreateCase;
    Boolean isConditionLogin1;
    Boolean isConditionLogin2;
}
