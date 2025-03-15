package vn.eledevo.vksbe.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.ResponseMessage;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class DocumentRequest {
    @NotBlank(message = ResponseMessage.FOLDER_NAME_NOT_BLANK)
    @Size(min = 1, max = 255, message = ResponseMessage.FOLDER_NAME_NOT_BLANK)
    String name;

    Long parentId;
}
