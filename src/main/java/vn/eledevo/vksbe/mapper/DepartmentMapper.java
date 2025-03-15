package vn.eledevo.vksbe.mapper;

import java.util.Objects;

import org.springframework.stereotype.Component;

import vn.eledevo.vksbe.dto.response.department.DepartmentResponse;
import vn.eledevo.vksbe.entity.Departments;

@Component
public class DepartmentMapper {
    // Private constructor
    private DepartmentMapper() {}

    public static DepartmentResponse toResponse(Departments e) {
        if (Objects.isNull(e)) {
            return new DepartmentResponse();
        }

        return DepartmentResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .code(e.getCode())
                .build();
    }
}
