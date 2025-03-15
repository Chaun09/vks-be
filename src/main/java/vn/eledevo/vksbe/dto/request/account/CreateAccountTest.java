package vn.eledevo.vksbe.dto.request.account;

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
public class CreateAccountTest {
    String username;
    String password;
    String pinCode;
}
