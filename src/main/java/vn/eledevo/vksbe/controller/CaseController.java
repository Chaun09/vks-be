package vn.eledevo.vksbe.controller;

import java.util.HashMap;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.CasePosition;
import vn.eledevo.vksbe.dto.model.account.AccountDownloadResponse;
import vn.eledevo.vksbe.dto.request.account_case.AccountDownloadRequest;
import vn.eledevo.vksbe.dto.request.case_flow.CaseFlowCreateRequest;
import vn.eledevo.vksbe.dto.request.case_flow.CaseFlowUpdateRequest;
import vn.eledevo.vksbe.dto.request.cases.*;
import vn.eledevo.vksbe.dto.request.cases.CaseCreateRequest;
import vn.eledevo.vksbe.dto.request.cases.CaseUpdateRequest;
import vn.eledevo.vksbe.dto.request.history.HistoryFilterRequest;
import vn.eledevo.vksbe.dto.response.*;
import vn.eledevo.vksbe.dto.response.account.StakeHolderResponse;
import vn.eledevo.vksbe.dto.response.account_case.AccountDownloadCaseResponse;
import vn.eledevo.vksbe.dto.response.case_flow.CaseFlowResponse;
import vn.eledevo.vksbe.dto.response.cases.CaseId;
import vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse;
import vn.eledevo.vksbe.dto.response.history.HistoryResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.service.account.AccountService;
import vn.eledevo.vksbe.service.case_flow.CaseFlowService;
import vn.eledevo.vksbe.service.cases.CaseService;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.service.mindmapTemplate.MindmapTemplateService;

@RestController
@RequestMapping("/api/v1/private/cases")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý vụ án")
public class CaseController {

    CaseService caseService;
    AccountService accountService;
    CaseFlowService caseFlowService;
    HistoryService historyService;
    MindmapTemplateService mindmapTemplateService;

    @GetMapping("/{id}/case-person/investigator")
    @Operation(summary = "Xem và tìm kiếm danh sách điều tra viên")
    public ApiResponse<ResponseFilter<CitizenCaseResponse>> getAllInvestigatorByCaseId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String textSearch,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize)
            throws ApiException {
        return ApiResponse.ok(caseService.getAllInvestigatorByCaseId(id, textSearch, page, pageSize));
    }

    @PatchMapping("/{id}/update")
    @Operation(summary = "Chỉnh sửa thông tin vụ án")
    public ApiResponse<HashMap<String, String>> updateCase(
            @PathVariable Long id, @RequestBody @Valid CaseUpdateRequest request)
            throws ApiException, ValidationException {
        return ApiResponse.ok(caseService.updateCase(id, request));
    }

    @GetMapping("/{id}/case-person/suspect-defendant")
    @Operation(summary = "Xem và tìm kiếm danh sách bị can hoặc bị cáo")
    public ApiResponse<ResponseFilter<CitizenCaseResponse>> getAllSuspectDefendantByCaseId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String textSearch,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize)
            throws ApiException {
        return ApiResponse.ok(caseService.getAllSuspectDefendantByCaseId(id, textSearch, page, pageSize));
    }

    @PatchMapping("/{id}/case-person/investigator/update")
    @Operation(summary = "Chỉnh sửa danh sách điều tra viên")
    public ApiResponse<HashMap<String, String>> updateInvestigatorByCase(
            @PathVariable Long id, @RequestBody @Valid CaseCitizenUpdateRequest request) throws ApiException {
        return ApiResponse.ok(caseService.updateInvestigator(id, request));
    }

    @PatchMapping("/{id}/account-cases/update")
    @Operation(summary = "Chỉnh sửa danh sách kiểm sát viên")
    public ApiResponse<HashMap<String, String>> updateProsecutorList(
            @RequestParam CasePosition type, @PathVariable Long id, @RequestBody @Valid CaseAccountRequest request)
            throws ApiException {
        return ApiResponse.ok(caseService.updateProsecutorList(type, id, request.getCasePersons()));
    }

    @PatchMapping("/{id}/case-person/suspect-defendant/update")
    @Operation(summary = "Chỉnh sửa danh sách bị can, bị cáo")
    public ApiResponse<HashMap<String, String>> updateSuspectAndDefendantByCase(
            @PathVariable Long id, @RequestBody @Valid CaseCitizenUpdateRequest request) throws ApiException {
        return ApiResponse.ok(caseService.updateSuspectAndDefendant(id, request));
    }

    @PatchMapping("/{id}/case-person/suspect-defendant/type/update")
    @Operation(summary = "Chỉnh sửa quyền bị can, bị cáo")
    public ApiResponse<HashMap<String, String>> updateTypeCasePersons(
            @PathVariable Long id, @RequestBody @Valid CaseCitizenUpdateRequest request) throws ApiException {
        return ApiResponse.ok(caseService.updateTypeCasePerson(id, request));
    }

    @PostMapping("/{id}/case-flow/create")
    @Operation(summary = "Tạo mới sơ đồ vụ án")
    public ApiResponse<CaseFlowResponse> createCaseFlow(
            @PathVariable Long id, @Valid @RequestBody CaseFlowCreateRequest caseFlowCreateRequest)
            throws ApiException {
        return ApiResponse.ok(caseFlowService.addCaseFlow(id, caseFlowCreateRequest));
    }

    @GetMapping("/{caseId}/case-flow")
    @Operation(summary = "xem sơ đồ vụ án")
    public ApiResponse<CaseFlowResponse> getCaseFlow(@PathVariable Long caseId) throws ApiException {
        return ApiResponse.ok(caseFlowService.getCaseFlow(caseId));
    }

    @PatchMapping("/{id}/case-flow/{idCaseFlow}")
    @Operation(summary = "Chỉnh sửa sơ đồ vụ án")
    public ApiResponse<HashMap<String, String>> update(
            @PathVariable Long id,
            @PathVariable Long idCaseFlow,
            @Valid @RequestBody CaseFlowUpdateRequest caseFlowUpdateRequest)
            throws Exception {
        return ApiResponse.ok(caseFlowService.updateCaseFlow(id, idCaseFlow, caseFlowUpdateRequest));
    }

    @GetMapping("/{caseId}/mindmap-template")
    @Operation(summary = "Xem danh sách sơ đồ mẫu trong vụ án")
    public ApiResponse<CaseMindmapTemplateResponse<MindmapTemplateResponse>> getAllMindMapTemplates(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "6") Integer pageSize,
            @RequestParam(required = false) String textSearch)
            throws ApiException {
        return ApiResponse.ok(caseService.getAllMindMapTemplates(caseId, page, pageSize, textSearch));
    }

    @GetMapping("/{caseId}/case-flow/{id}")
    @Operation(summary = "xem chi tiết sơ đồ vụ án")
    public ApiResponse<CaseFlowResponse> getDetailCaseFlow(@PathVariable Long caseId, @PathVariable Long id)
            throws ApiException {
        return ApiResponse.ok(caseFlowService.getDetailCaseFlow(caseId, id));
    }

    @PostMapping("/{id}/case-flow/upload-image")
    @Operation(summary = "Upload ảnh preview sơ đồ")
    public ApiResponse<ResultUrl> uploadImg(@PathVariable Long id, @RequestParam MultipartFile file) throws Exception {

        return ApiResponse.ok(caseFlowService.uploadImg(file, id));
    }

    @GetMapping("/{id}/accounts-no-permission-download")
    @Operation(summary = "Xem danh sách tài khoản không có quyền download trong vụ án - CAS-028")
    public ApiResponse<ResultList<AccountDownloadResponse>> getListAccountNoPermissionDownload(
            @PathVariable Long id, @RequestParam(required = false) String textSearch) throws ApiException {
        return ApiResponse.ok(caseService.getListAccountNoPermissionDownload(id, textSearch));
    }

    @PatchMapping("/{id}/accounts{accountId}/remove-permission-download")
    @Operation(summary = "Gỡ quyền download trong vụ án - CAS-029")
    public ApiResponse<HashMap<String, String>> removePermissionDownloadCase(
            @PathVariable(name = "id") Long caseId, @PathVariable Long accountId) throws ApiException {
        return ApiResponse.ok(caseService.removePermissionDownloadCase(caseId, accountId));
    }

    @PatchMapping("/{id}/accounts/grant-permission-download")
    @Operation(summary = "Cấp quyền download cho tài khoản trong vụ án - CAS-030")
    public ApiResponse<ResultList<AccountDownloadCaseResponse>> grantPermissionDownloadCase(
            @PathVariable(name = "id") Long caseId, @RequestBody AccountDownloadRequest request) throws ApiException {
        return ApiResponse.ok(caseService.grantPermissionDownloadCase(caseId, request.getAccountIds()));
    }

    @GetMapping("/{id}/accounts-has-permission-download")
    @Operation(summary = "Xem danh sách tài khoản có quyền download vụ án - CAS-027")
    public ApiResponse<ResultList<AccountDownloadResponse>> getListAccountHasPermissionDownload(@PathVariable Long id)
            throws ApiException {
        return ApiResponse.ok(caseService.getListAccountHasPermissionDownload(id));
    }

    @PostMapping("/{id}/history")
    @Operation(summary = "Xem lịch sử thay đổi vụ án")
    public ApiResponse<ResponseFilter<HistoryResponse>> getHistoryCase(
            @PathVariable Long id,
            @RequestParam Long page,
            @RequestParam Long pageSize,
            @RequestBody HistoryFilterRequest request)
            throws ApiException {
        return ApiResponse.ok(historyService.getHistoryCase(request, id, page, pageSize));
    }

    @GetMapping("/{id}/accounts")
    @Operation(summary = "Xem danh sách lãnh đạo phụ trách")
    public ApiResponse<ResponseFilter<Page>> getAllStakeHolder(
            @PathVariable Long id, @RequestParam int page, @RequestParam int pageSize) throws ApiException {

        return ApiResponse.ok(caseService.getAllStakeHolderByCaseId(id, page, pageSize));
    }

    @PostMapping("/create")
    @Operation(summary = "Taọ mới vụ án")
    public ApiResponse<CaseId> createCase(CaseCreateRequest caseCreateRequest) throws ApiException {
        CaseId caseId = new CaseId();
        //        if(caseService.createCase(caseCreateRequest) == null)
        //        {
        //            throw new ApiException(CaseErrorCode.CASE_NOT_ACCESS);
        //        }
        //        else {
        caseId.setId(caseService.createCase(caseCreateRequest));
        //        }
        return ApiResponse.ok(caseId);
    }
}
