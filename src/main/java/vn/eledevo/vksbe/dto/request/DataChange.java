package vn.eledevo.vksbe.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DataChange {
    String maPin;
    String keyUsb;
    String[] listDevices;
    String usbCode;
    String usbVendorCode;
}
