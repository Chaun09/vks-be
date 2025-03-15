package vn.eledevo.vksbe.dto.model.usb;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class UsbInfoToEncrypt {
    String pin;
    String keyUsb;
    List<String> listDevices;
    String usbCode;
    String usbVendorCode;
}
