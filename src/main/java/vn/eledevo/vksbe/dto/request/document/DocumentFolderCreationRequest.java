package vn.eledevo.vksbe.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Trimmed
public class DocumentFolderCreationRequest {
    @NotBlank(message = "Tên thư mục không được để trống")
    @Size(max = 255, message = "Tên thư mục chỉ có kích thước tối đa là 255 kí tự")
    String name;

    @NotNull(message = "ID của thư mục cha không được để trống")
    Long parentId;
}
