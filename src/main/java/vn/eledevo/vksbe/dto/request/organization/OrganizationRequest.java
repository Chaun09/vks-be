package vn.eledevo.vksbe.dto.request.organization;

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
public class OrganizationRequest {
    @NotBlank(message = ResponseMessage.ORGANIZATION_NAME_NOT_BLANK)
    @Size(min = 4, max = 255, message = ResponseMessage.ORGANIZATION_NAME_SIZE)
    String name;

    @NotBlank(message = ResponseMessage.ORGANIZATION_CODE_NOT_BLANK)
    @Size(min = 4, max = 8, message = ResponseMessage.ORGANIZATION_CODE_SIZE)
    String code;

    @NotBlank(message = ResponseMessage.ORGANIZATION_ADDRESS_NOT_BLANK)
    @Size(max = 225, message = ResponseMessage.ORGANIZATION_ADDRESS_SIZE)
    String address;
}
