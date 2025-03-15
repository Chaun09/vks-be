package vn.eledevo.vksbe.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@Trimmed
public class AccountActive {
    Long id;
    String roleCode;
    String status;
    Long departmentId;
}
