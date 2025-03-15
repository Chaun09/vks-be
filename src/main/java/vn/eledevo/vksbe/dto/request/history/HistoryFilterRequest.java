package vn.eledevo.vksbe.dto.request.history;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class HistoryFilterRequest {
    String textSearch;
    LocalDate fromDate;
    LocalDate toDate;
}
