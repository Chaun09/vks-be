package vn.eledevo.vksbe.dto.response.usb;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsbResponse {
    Long id;
    String usbCode;
    String usbVendorCode;
    String name;
    String keyUsb;
    String status;
    Long createdAt;
    Long updatedAt;
    String createdBy;
    String updatedBy;
}
