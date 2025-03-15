package vn.eledevo.vksbe.dto.response.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountSwapResponse {
    Long id;
    String username;
    String fullName;
}
