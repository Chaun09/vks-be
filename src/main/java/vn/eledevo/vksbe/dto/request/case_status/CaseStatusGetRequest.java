package vn.eledevo.vksbe.dto.request.case_status;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class CaseStatusGetRequest {
    String name;
    LocalDate fromDate;
    LocalDate toDate;
}
