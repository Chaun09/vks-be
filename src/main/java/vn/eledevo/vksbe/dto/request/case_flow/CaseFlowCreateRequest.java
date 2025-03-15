package vn.eledevo.vksbe.dto.request.case_flow;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseFlowCreateRequest {
    Long mindmapTemplateId;
}
