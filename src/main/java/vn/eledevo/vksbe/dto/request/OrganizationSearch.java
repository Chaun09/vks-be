package vn.eledevo.vksbe.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import vn.eledevo.vksbe.utils.TimeUtils;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Trimmed
public class OrganizationSearch {
    String name;
    String code;
    String address;
    LocalDate fromDate;
    LocalDate toDate;

    public LocalDateTime getFromDateTime() {
        return TimeUtils.toLocalDateTimeStart(fromDate);
    }

    public LocalDateTime getToDateTime() {
        return TimeUtils.toLocalDateTimeEnd(toDate);
    }
}
