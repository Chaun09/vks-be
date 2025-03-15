package vn.eledevo.vksbe.dto.request.department;

import jakarta.validation.constraints.NotBlank;
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
public class UpdateDepartment {
    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(min = 1, max = 255, message = "Tên phòng ban không được vượt quá 255 ký tự")
    String departmentName;
}
