package vn.eledevo.vksbe.dto.response.dashboard;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseDataDashBoardResponse {
    List<MonthCase> listMonthCase;
    List<DepartmentCase> listDepartmentCase;
    Long totalCase;
    Long totalCivilCase;
    Long totalCriminalCase;
}
