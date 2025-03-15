package vn.eledevo.vksbe.dto.request.cases;

import static vn.eledevo.vksbe.constant.ResponseMessage.*;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.CaseType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseCreateRequest {
    @NotBlank(message = CASE_NAME_CANNOT_BE_BLANK)
    @Pattern(regexp = "^[a-zA-Z0-9\\sÀ-ỹà-ỹ]*$", message = CASE_NAME_SPECIAL)
    @Size(max = 255, message = CASE_NAME_CANNOT_EXCEED_255_CHARACTER)
    String name;

    @NotBlank(message = CASE_CODE_CANNOT_BE_BLANK)
    @Pattern(regexp = "^[a-zA-Z0-9\\sÀ-ỹà-ỹ]*$", message = CASE_CODE_SPECIAL)
    @Size(max = 255, message = CASE_CODE_CANNOT_EXCEED_255_CHARACTER)
    String code;

    @NotNull(message = DEPARTMENT_ID_NOT_NULL)
    Long departmentId;

    @NotBlank(message = DEPARTMENT_NAME_NOT_NULL)
    String departmentName;

    CaseType type;
    String description;

    @NotNull(message = ACTUAL_TIME_NOT_NULL)
    LocalDate actualTime;
}
