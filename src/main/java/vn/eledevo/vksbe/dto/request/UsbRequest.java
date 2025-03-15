package vn.eledevo.vksbe.dto.request;

import java.time.LocalDate;

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
public class UsbRequest {
    String usbCode;
    String createByAccountName;
    String status;
    LocalDate fromDate;
    LocalDate toDate;
}
