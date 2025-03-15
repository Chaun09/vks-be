package vn.eledevo.vksbe.dto.model.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDownloadResponse {
    Long id;
    String username;
    String fullName;
    String roleName;
    String status;
}
