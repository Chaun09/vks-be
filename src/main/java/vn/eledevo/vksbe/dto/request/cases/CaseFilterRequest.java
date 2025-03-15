package vn.eledevo.vksbe.dto.request.cases;

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
public class CaseFilterRequest {
    String textSearch;
    Long userInChargeId;
    Long prosecutorId;
    Long citizenId;
    LocalDate fromDate;
    LocalDate toDate;
    Long departmentId;
    String departmentName;
    Long statusId;
}
