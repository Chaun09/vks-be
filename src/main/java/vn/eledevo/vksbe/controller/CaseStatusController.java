package vn.eledevo.vksbe.controller;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.request.case_status.CaseStatusCreateRequest;
import vn.eledevo.vksbe.dto.request.case_status.CaseStatusGetRequest;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.case_status.CaseStatusResponse;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.service.case_status.CaseStatusService;

@RestController
@RequestMapping("/api/v1/private/case-status")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý trạng thái vụ án")
public class CaseStatusController {
    CaseStatusService caseStatusService;

    @PostMapping("/create")
    @Operation(summary = "Thêm mới trạng thái vụ án")
    public ApiResponse<Map<String, String>> createCaseStatus(
            @Valid @RequestBody CaseStatusCreateRequest caseStatusCreateRequest)
            throws ApiException, ValidationException {
        return ApiResponse.ok(caseStatusService.createCaseStatus(caseStatusCreateRequest));
    }

    @PostMapping("")
    @Operation(summary = "Xem và tìm kiếm trạng thái của vụ án")
    public ApiResponse<ResponseFilter<CaseStatusResponse>> getCaseStatus(
            @RequestBody CaseStatusGetRequest caseStatusGetRequest,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize)
            throws ApiException {
        return ApiResponse.ok(caseStatusService.getCaseStatus(caseStatusGetRequest, page, pageSize));
    }

    @PatchMapping("/{id}/update")
    @Operation(summary = "Chỉnh sửa trạng thái vụ án")
    public ApiResponse<Map<String, String>> updateCaseStatus(
            @PathVariable Long id, @Valid @RequestBody CaseStatusCreateRequest caseStatusCreateRequest)
            throws ApiException, ValidationException {
        return ApiResponse.ok(caseStatusService.updateCaseStatus(id, caseStatusCreateRequest));
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "Xem thông tin chi tiết trạng thái")
    public ApiResponse<CaseStatusResponse> getCaseStatus(@PathVariable("id") Long id) throws ApiException {
        return ApiResponse.ok(caseStatusService.getCaseStatusDetail(id));
    }
}
