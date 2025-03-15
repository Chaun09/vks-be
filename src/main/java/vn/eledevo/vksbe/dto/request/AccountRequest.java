package vn.eledevo.vksbe.dto.request;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class AccountRequest {
    String username;
    String fullName;
    Long roleId;
    String roleName;
    Long departmentId;
    String departmentName;
    Long organizationId;
    String organizationName;
    String status;
    LocalDate fromDate;
    LocalDate toDate;
}
