package vn.eledevo.vksbe.dto.request.account;

import static vn.eledevo.vksbe.constant.ResponseMessage.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.RegexPattern;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;
import vn.eledevo.vksbe.utils.ValidIds;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ValidIds(fields = {"roleId", "departmentId", "organizationId"})
@Trimmed
public class AccountUpdateRequest {
    @NotBlank(message = USERNAME_REQUIRE)
    @Size(min = 1, max = 255, message = USERNAME_SIZE)
    String username;

    @NotBlank(message = FULL_NAME_REQUIRE)
    @Size(min = 1, max = 255, message = FULL_NAME_SIZE)
    String fullName;

    String avatar;

    @NotBlank(message = GENDER_NOT_BLANK)
    String gender;

    Long roleId;

    String roleName;

    Long departmentId;

    String departmentName;

    Long organizationId;

    String organizationName;

    @NotBlank(message = PHONE_NUMBER_REQUIRE)
    @Pattern(regexp = RegexPattern.PHONE_NUMBER, message = PHONE_NUMBER_INVALID)
    String phoneNumber;

    Long swappedAccId;
}
