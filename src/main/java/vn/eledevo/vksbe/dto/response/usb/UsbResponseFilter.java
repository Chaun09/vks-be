package vn.eledevo.vksbe.dto.response.usb;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsbResponseFilter {
    Long id;
    String name;
    String usbCode;
    String usbVendorCode;
    String keyUsb;
    String status;
    LocalDate createdAt;
    LocalDate updatedAt;
    String createdBy;
    String updatedBy;
    String accountFullName;

    public UsbResponseFilter(
            Long id,
            String name,
            String usbCode,
            String usbVendorCode,
            String keyUsb,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String createdBy,
            String updatedBy,
            String accountFullName) {
        this.id = id;
        this.name = name;
        this.usbCode = usbCode;
        this.usbVendorCode = usbVendorCode;
        this.keyUsb = keyUsb;
        this.status = status;
        this.createdAt = createdAt.toLocalDate();
        this.updatedAt = updatedAt.toLocalDate();
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.accountFullName = accountFullName;
    }
}
