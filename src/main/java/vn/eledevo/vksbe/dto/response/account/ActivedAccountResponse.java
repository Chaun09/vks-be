package vn.eledevo.vksbe.dto.response.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivedAccountResponse {
    AccountSwapResponse oldPositionAccInfo;
    String message;
}
