package vn.eledevo.vksbe.dto.request.usb;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class UsbTokenInfo {
    @NotNull(message = "usbCode không được để trống.")
    @NotBlank(message = "usbCode không được để trống.")
    String usbCode;

    @NotNull(message = "usbVendorCode không được để trống.")
    @NotBlank(message = "usbVendorCode không được để trống.")
    String usbVendorCode;

    @NotNull(message = "nameUsb không được để trống.")
    @NotBlank(message = "nameUsb không được để trống.")
    String nameUsb;
}
