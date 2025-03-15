package vn.eledevo.vksbe.dto.response.citizen;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CitizenResponse {
    Long id;
    String name;
    String profileImage;
    String citizenId;
}
