package vn.eledevo.vksbe.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthCase {
    Integer month;
    Long totalCivilCase;
    Long totalCriminalCase;
}
