package vn.eledevo.vksbe.dto.response.citizen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.Gender;
import vn.eledevo.vksbe.constant.RegexPattern;
import vn.eledevo.vksbe.constant.ResponseMessage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CitizenUpdateRequest {
    @NotBlank(message = ResponseMessage.CITIZEN_NAME_NOT_BLANK)
    @Size(max = 225, message = ResponseMessage.CITIZEN_NAME_SIZE)
    @Pattern(
            regexp = RegexPattern.SPECIAL_CHARACTER_DETECTION_PATTERN,
            message = ResponseMessage.SPECIAL_CHARACTER_DETECTION_PATTERN)
    String name;

    @NotNull(message = "Giới tính không được để trống")
    Gender gender;

    @Size(max = 225, message = ResponseMessage.ORGANIZATION_ADDRESS_SIZE)
    String address;

    @Size(max = 1000, message = ResponseMessage.CITIZEN_PROFILE_IMAGE_SIZE)
    String profileImage;

    @Size(max = 225, message = ResponseMessage.CITIZEN_WORKINGADDRESS_SIZE)
    String workingAddress;

    @Size(max = 225, message = ResponseMessage.CITIZEN_JOB_SIZE)
    String job;
}
