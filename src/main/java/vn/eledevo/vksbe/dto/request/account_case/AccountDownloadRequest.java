package vn.eledevo.vksbe.dto.request.account_case;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDownloadRequest {
    Set<Long> accountIds;
}
