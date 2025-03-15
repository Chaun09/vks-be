package vn.eledevo.vksbe.controller;

import java.util.HashMap;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.model.account.UserInfo;
import vn.eledevo.vksbe.dto.request.AccountRequest;
import vn.eledevo.vksbe.dto.request.PinChangeRequest;
import vn.eledevo.vksbe.dto.request.account.*;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.ResultList;
import vn.eledevo.vksbe.dto.response.ResultUrl;
import vn.eledevo.vksbe.dto.response.account.*;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponse;
import vn.eledevo.vksbe.dto.response.computer.ConnectComputerResponse;
import vn.eledevo.vksbe.dto.response.usb.UsbConnectedResponse;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.service.account.AccountService;
import vn.eledevo.vksbe.service.organizational_structure.OrganizationalStructureService;

@RestController
@RequestMapping("/api/v1/private/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý tài khoản")
public class AccountController {
    AccountService accountService;
    OrganizationalStructureService organizationalStructureUtilsService;

    @PatchMapping("/reset-password/{id}")
    @Operation(summary = "Reset mật khẩu")
    public ApiResponse<HashMap<String, String>> resetPassword(
            @Parameter(description = "ID of the user", required = true) @PathVariable Long id) throws ApiException {
        return ApiResponse.ok(accountService.resetPassword(id));
    }

    @GetMapping("/{id}/devices")
    @Operation(summary = "Lấy danh sách thiết bị đã liên kết với tài khoản")
    public ApiResponse<ResultList<ComputerResponse>> getComputerListByAccountId(
            @Parameter(description = "ID of the  user", required = true) @PathVariable Long id) throws ApiException {
        return ApiResponse.ok(accountService.getComputersByIdAccount(id));
    }

    @PostMapping()
    @Operation(summary = "Xem danh sách tài khoản")
    public ApiResponse<ResponseFilter<AccountResponseByFilter>> getAccountList(
            @RequestBody AccountRequest req,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
            throws ApiException {
        organizationalStructureUtilsService.validate(req);
        return ApiResponse.ok(accountService.getListAccountByFilter(req, page, pageSize));
    }

    @PatchMapping("/{accountId}/inactivate")
    @Operation(summary = "Khóa tài khoản")
    public ApiResponse<HashMap<String, String>> lockAccount(@PathVariable Long accountId) throws ApiException {
        return ApiResponse.ok(accountService.inactivateAccount(accountId));
    }

    @GetMapping("/{id}/usb")
    @Operation(summary = "Xem thông tin USB liên kết với tài khoản")
    public ApiResponse<ResultList<UsbConnectedResponse>> getUsbInfo(
            @Parameter(description = "ID of the user", required = true) @PathVariable Long id) throws ApiException {
        return ApiResponse.ok(accountService.getUsbInfo(id));
    }

    @PatchMapping("/connect-computer/{id}/computers")
    @Operation(summary = "Thêm liên kết thiết bị với tài khoản (trả về danh sách kết nối)")
    public ApiResponse<ResultList<ConnectComputerResponse>> connectComputers(
            @PathVariable("id") Long accountId,
            @RequestBody @NotEmpty(message = "Danh sách kết nối không được rỗng") Set<Long> computerIds)
            throws ApiException {
        return ApiResponse.ok(accountService.connectComputers(accountId, computerIds));
    }

    @PatchMapping("/remove-usb/{accountId}/usb/{usbId}")
    @Operation(summary = "Gỡ USB kết nối với tài khoản")
    public ApiResponse<HashMap<String, String>> removeUsb(@PathVariable Long accountId, @PathVariable Long usbId)
            throws ApiException {
        return ApiResponse.ok(accountService.removeConnectUSB(accountId, usbId));
    }

    @PatchMapping("/{idAccount}/activate")
    @Operation(summary = "Kích hoạt tài khoản")
    public ApiResponse<ActivedAccountResponse> activateAccount(
            @Parameter(description = "ID of the user", required = true) @PathVariable Long idAccount)
            throws ApiException {
        return ApiResponse.ok(accountService.activeAccount(idAccount));
    }

    @PatchMapping("/{accountId}/swap-account-status/{swapAccountId}")
    @Operation(summary = "Swap trạng thái tài khoản")
    public ApiResponse<AccountSwapResponse> swapAccountSattus(
            @PathVariable Long accountId, @PathVariable Long swapAccountId) throws ApiException {
        return ApiResponse.ok(accountService.swapStatus(accountId, swapAccountId));
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar")
    public ApiResponse<ResultUrl> uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.ok(accountService.uploadAvatar(file));
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo mới tài khoản")
    public ApiResponse<AccountResponse> createAccount(@RequestBody @Valid AccountCreateRequest request)
            throws ApiException, ValidationException {
        return ApiResponse.ok(accountService.createAccountInfo(request));
    }

    @PatchMapping("/{accountId}/remove-computer/{computerId}")
    @Operation(summary = "Gỡ thiết bị máy tính đã liên kết với tài khoản")
    public ApiResponse<HashMap<String, String>> removeComputer(
            @PathVariable Long accountId, @PathVariable Long computerId) throws ApiException {
        return ApiResponse.ok(accountService.removeConnectComputer(accountId, computerId));
    }

    @PatchMapping("/{updatedAccId}/update-info")
    @Operation(summary = "Chỉnh sửa thông tin tài khoản")
    public ApiResponse<AccountSwapResponse> updateAccountInfo(
            @PathVariable(value = "updatedAccId") Long updatedAccId, @Valid @RequestBody AccountUpdateRequest req)
            throws Exception {
        organizationalStructureUtilsService.validateUpdate(req);
        return ApiResponse.ok(accountService.updateAccountInfo(updatedAccId, req));
    }

    @GetMapping("/get-user-info")
    @Operation(summary = "Thông tin cá nhân của tài khoản đăng nhập")
    public ApiResponse<UserInfo> userDetail() throws ApiException {
        return ApiResponse.ok(accountService.userInfo());
    }

    @PatchMapping("/{id}/update-avatar-user-info")
    @Operation(summary = "Chỉnh sửa avatar của tài khoản đăng nhập")
    public ApiResponse<AccountResponse> updateAvatarUserInfo(@PathVariable Long id, @RequestBody AvatarRequest request)
            throws Exception {
        return ApiResponse.ok(accountService.updateAvatarUserInfo(id, request));
    }

    @PostMapping("/change-pin-code")
    @Operation(summary = "Thay đổi mã PIN của tài khoản đang đăng nhập")
    public ApiResponse<HashMap<String, String>> changePinCodeUserLogin(@Valid @RequestBody PinChangeRequest pinRequest)
            throws ApiException, ValidationException {
        return ApiResponse.ok(accountService.changePinUserLogin(pinRequest));
    }

    @PatchMapping("/{id}/grant-permission-create-case")
    @Operation(summary = "Gán/gỡ quyền tạo vụ án  ")
    public ApiResponse<HashMap<String, String>> grantPermissionToCreateCase(
            @PathVariable Long id, @RequestBody GrantedPermissionRequest req) throws ApiException {
        return ApiResponse.ok(accountService.grantPermissionToCreateCase(id, req));
    }
}
