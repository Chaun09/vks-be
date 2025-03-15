package vn.eledevo.vksbe.dto.response.account;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.request.DataChange;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Token2FAResponse {
    DataChange hasString;
    long createdTokenTime;
    long expiredTime;
}
