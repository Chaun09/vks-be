package vn.eledevo.vksbe.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.service.case_offline.CaseOfflineService;

@RestController
@RequestMapping("/api/v1/private/case-offline")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý vụ án offline")
public class CaseOfflineController {
    CaseOfflineService caseOfflineService;

    @GetMapping("/{id}/uri-name")
    @Operation(summary = "Lấy thông tin uriName")
    public ApiResponse<List<String>> getUriName(
            @Parameter(description = "ID of the case", required = true) @PathVariable Long id) throws ApiException {
        return ApiResponse.ok(caseOfflineService.getUriName(id));
    }
}
