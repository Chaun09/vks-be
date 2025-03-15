package vn.eledevo.vksbe.dto.request.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrantedPermissionRequest {
    Boolean isCreateCase;
}
