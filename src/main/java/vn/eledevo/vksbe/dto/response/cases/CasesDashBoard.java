package vn.eledevo.vksbe.dto.response.cases;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CasesDashBoard {
    private Integer year;
    private Integer month;
    private Integer count;
}
