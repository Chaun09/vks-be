package vn.eledevo.vksbe.dto.response.document;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DocumentParentResponse {
    Long id;
    String name;
    String type;
}
