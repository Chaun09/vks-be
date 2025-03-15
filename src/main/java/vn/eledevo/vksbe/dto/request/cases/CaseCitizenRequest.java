package vn.eledevo.vksbe.dto.request.cases;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.CasePersonType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseCitizenRequest {
    Long id;
    Boolean isChecked;
    CasePersonType type;
}
