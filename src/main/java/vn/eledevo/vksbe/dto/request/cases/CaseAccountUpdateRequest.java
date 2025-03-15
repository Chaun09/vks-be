package vn.eledevo.vksbe.dto.request.cases;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseAccountUpdateRequest {
    Long id;
    Boolean isChecked;
}
