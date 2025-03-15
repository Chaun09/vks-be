package vn.eledevo.vksbe.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String accessToken;
    String refreshToken;
    String usbCode;
    String usbVendorCode;
    String roleName;
    String departmentName;
    Boolean isConditionLogin1;
    Boolean isConditionLogin2;
}
