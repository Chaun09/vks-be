package vn.eledevo.vksbe.dto.request.case_status;

import static vn.eledevo.vksbe.constant.RegexPattern.CASE_STATUS_NAME;
import static vn.eledevo.vksbe.constant.ResponseMessage.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class CaseStatusCreateRequest {
    @NotBlank(message = CASE_STATUS_NAME_CANNOT_BE_BLANK)
    @Pattern(regexp = CASE_STATUS_NAME, message = STATUS_NAME_CAN_ONLY_CONTAIN_LETTER)
    @Size(max = 255, message = CASE_STATUS_NAME_CANNOT_EXCEED_255_CHARACTER)
    String name;

    String description;
}
