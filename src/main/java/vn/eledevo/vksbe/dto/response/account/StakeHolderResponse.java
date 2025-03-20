package vn.eledevo.vksbe.dto.response.account;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StakeHolderResponse {
    private Long id;
    private String username;
    private String roleName;
    private String status;
    private String fullname;
    private String avatar;
    private Long role_id;
    private Long account_id;
}
