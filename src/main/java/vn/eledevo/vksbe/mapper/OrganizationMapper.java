package vn.eledevo.vksbe.mapper;

import java.util.Objects;

import vn.eledevo.vksbe.dto.response.organization.OrganizationResponse;
import vn.eledevo.vksbe.entity.Organizations;

public class OrganizationMapper {
    // Private constructor
    private OrganizationMapper() {}

    public static OrganizationResponse toResponse(Organizations e) {
        if (Objects.isNull(e)) {
            return new OrganizationResponse();
        }

        return OrganizationResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .code(e.getCode())
                .address(e.getAddress())
                .build();
    }
}
