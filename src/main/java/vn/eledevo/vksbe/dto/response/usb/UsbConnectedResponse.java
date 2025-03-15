package vn.eledevo.vksbe.dto.response.usb;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsbConnectedResponse {
    Long id;
    String name;
    String usbCode;
    LocalDate createdAt;
    String usbVendorCode;

    public UsbConnectedResponse(Long id, String name, String usbCode, LocalDateTime createdAt, String usbVendorCode) {
        this.id = id;
        this.name = name;
        this.usbCode = usbCode;
        this.createdAt = createdAt.toLocalDate();
        this.usbVendorCode = usbVendorCode;
    }
}
