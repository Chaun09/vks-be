package vn.eledevo.vksbe.dto.request.cases;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseAccountRequest {
    List<CaseAccountUpdateRequest> casePersons;
}
