package vn.eledevo.vksbe.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Trimmed
public class TwoFactorAuthenticationRequest {
    String tokenUsb;
    String currentUsbCode;
    String currentUsbVendorCode;
    String currentDeviceId;
}
