package vn.eledevo.vksbe.dto.response.account_case;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDownloadCaseResponse {
    Long id;
    String username;
    String fullName;
    Boolean hasPermission;
    String reason;
}
