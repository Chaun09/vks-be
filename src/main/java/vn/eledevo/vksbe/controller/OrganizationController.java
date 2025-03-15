package vn.eledevo.vksbe.controller;

import java.util.HashMap;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.request.OrganizationSearch;
import vn.eledevo.vksbe.dto.request.organization.OrganizationRequest;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.organization.OrganizationResponse;
import vn.eledevo.vksbe.entity.Organizations;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.service.organization.OrganizationService;

@RestController
@RequestMapping("/api/v1/private/organizations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý đơn vị")
public class OrganizationController {
    OrganizationService organizationService;

    @PostMapping("/search")
    @Operation(summary = "Xem và tìm kiếm đơn vị")
    public ApiResponse<ResponseFilter<OrganizationResponse>> getOrganizationList(
            @RequestBody OrganizationSearch organizationSearch,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize)
            throws ApiException {
        return ApiResponse.ok(organizationService.getOrganizationList(organizationSearch, page, pageSize));
    }

    @PatchMapping("/{id}/update")
    @Operation(summary = "Chỉnh sửa đơn vị")
    public ApiResponse<Organizations> updateOrganization(
            @PathVariable Long id, @Valid @RequestBody OrganizationRequest organizationRequest)
            throws ApiException, ValidationException {
        return ApiResponse.ok(organizationService.updateOrganization(id, organizationRequest));
    }

    @PatchMapping("/{id}/detail")
    @Operation(summary = "Lấy chi tiết đơn vị từ danh sách đơn vị")
    public ApiResponse<OrganizationResponse> getOrganizationDetail(@PathVariable Long id) throws ApiException {
        return ApiResponse.ok(organizationService.getOrganizationDetail(id));
    }

    @DeleteMapping("/{id}/delete")
    @Operation(summary = "xóa đơn vị theo id")
    public ApiResponse<HashMap<String, String>> deleteOrganization(@PathVariable Long id) throws ApiException {
        return ApiResponse.ok(organizationService.deleteOrganization(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo mới đơn vị")
    public ApiResponse<HashMap<String, String>> createOrganization(
            @Valid @RequestBody OrganizationRequest organizationRequest) throws ApiException {
        return ApiResponse.ok(organizationService.createOrganization(organizationRequest));
    }
}
