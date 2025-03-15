package vn.eledevo.vksbe.dto.request.mindmapTemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.ResponseMessage;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class MindmapTemplateUpdateRequest {
    @NotNull(message = ResponseMessage.DEPARTMENT_ID_NOT_NULL)
    Long departmentId;

    @NotNull(message = ResponseMessage.DEPARTMENT_NAME_NOT_NULL)
    @Size(max = 255, message = ResponseMessage.NAMEMINDMAP_MAX)
    String departmentName;

    @NotBlank(message = ResponseMessage.MINDMAPTEMPLATE_NOT_NULL)
    @Size(max = 255, message = ResponseMessage.NAMEMINDMAP_MAX)
    //    @Pattern(regexp = MINDMAP_NAME, message = ResponseMessage.NAMEMINDMAP_SPECIAL)
    String name;

    String dataLink;
    String dataNode;
    String url;
}
