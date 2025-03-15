package vn.eledevo.vksbe.dto.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.response.department.DepartmentResponse;
import vn.eledevo.vksbe.dto.response.organization.OrganizationResponse;
import vn.eledevo.vksbe.dto.response.role.RoleResponse;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class InformationResponse {
    List<DepartmentResponse> departments;
    List<OrganizationResponse> organizations;
    List<RoleResponse> roles;
}
