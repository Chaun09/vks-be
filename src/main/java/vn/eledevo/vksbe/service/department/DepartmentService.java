package vn.eledevo.vksbe.service.department;

import java.util.HashMap;

import vn.eledevo.vksbe.dto.request.department.UpdateDepartment;
import vn.eledevo.vksbe.dto.response.ResultList;
import vn.eledevo.vksbe.dto.response.department.DepartmentResponse;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;

public interface DepartmentService {
    Boolean departmentNameChangeDetector(Long departmentId, String departmentName);

    ResultList<DepartmentResponse> getDepartmentList();

    HashMap<String, String> updateDepartment(Long departmentId, UpdateDepartment departmentRequest)
            throws ApiException, ValidationException;
}
