package vn.eledevo.vksbe.dto.request.cases;

import static vn.eledevo.vksbe.constant.ResponseMessage.*;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class CaseUpdateRequest {
    @Size(min = 1, max = 255, message = "Tên vụ án có kích thước từ 1 đến 255 kí tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\sÀ-ỹà-ỹ]*$", message = CASE_NAME_SPECIAL)
    String name;

    @Size(min = 1, max = 255, message = "Mã vụ án có kích thước từ 1 đến 255 kí tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\sÀ-ỹà-ỹ]*$", message = CASE_CODE_SPECIAL)
    String code;

    Long statusId;

    @Size(min = 1, max = 255, message = "Kiểu vụ án có kích thước từ 1 đến 255 kí tự")
    String type;

    @Size(max = 255, message = "Mô tả có kích thước tối đa là 255 kí tự")
    String description;

    @NotNull(message = ACTUAL_TIME_NOT_NULL)
    LocalDate actualTime;
}
