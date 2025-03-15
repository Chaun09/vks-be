package vn.eledevo.vksbe.dto.request.cases;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseCitizenUpdateRequest {
    List<CaseCitizenRequest> listCitizens;
}
