package vn.eledevo.vksbe.dto.request.account;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TimeUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCaseRequest {
    String textSearch;
    Long userInChargeId;
    Long prosecutorId;
    Long citizenId;
    Long statusId;
    String hasAccess;
    Long departmentId;
    String departmentName;
    LocalDate fromDate;
    LocalDate toDate;

    public LocalDateTime getTimeFromDate() {
        return TimeUtils.toLocalDateTimeStart(this.fromDate);
    }

    public LocalDateTime getTimeToDate() {
        return TimeUtils.toLocalDateTimeEnd(this.toDate);
    }
}
