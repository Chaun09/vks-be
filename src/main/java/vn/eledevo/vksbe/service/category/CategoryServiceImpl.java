package vn.eledevo.vksbe.service.category;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.response.InformationResponse;
import vn.eledevo.vksbe.dto.response.department.DepartmentResponse;
import vn.eledevo.vksbe.dto.response.organization.OrganizationResponse;
import vn.eledevo.vksbe.dto.response.role.RoleResponse;
import vn.eledevo.vksbe.entity.Organizations;
import vn.eledevo.vksbe.mapper.DepartmentMapper;
import vn.eledevo.vksbe.mapper.RoleMapper;
import vn.eledevo.vksbe.repository.DepartmentRepository;
import vn.eledevo.vksbe.repository.OrganizationRepository;
import vn.eledevo.vksbe.repository.RoleRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    DepartmentRepository departmentRepository;

    OrganizationRepository organizationRepository;

    RoleRepository roleRepository;

    @Override
    public InformationResponse getAllInformation() {
        List<DepartmentResponse> departmentsList = departmentRepository.findAll().stream()
                .map(DepartmentMapper::toResponse)
                .toList();

        Organizations organizations = organizationRepository.findByCode("VKS00001");
        OrganizationResponse organizationResponse = OrganizationResponse.builder()
                .id(organizations.getId())
                .code(organizations.getCode())
                .name(organizations.getName())
                .address(organizations.getAddress())
                .isDefault(organizations.getIsDefault())
                .updatedAt(organizations.getUpdatedAt().toLocalDate())
                .build();
        List<OrganizationResponse> organizationsList = new ArrayList<>();
        organizationsList.add(organizationResponse);

        List<RoleResponse> rolesList =
                roleRepository.findAll().stream().map(RoleMapper::toResponse).toList();

        return InformationResponse.builder()
                .departments(departmentsList)
                .organizations(organizationsList)
                .roles(rolesList)
                .build();
    }
}
