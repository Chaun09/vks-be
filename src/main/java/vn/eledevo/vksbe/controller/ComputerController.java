package vn.eledevo.vksbe.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.model.computer.ComputersModel;
import vn.eledevo.vksbe.dto.request.ComputerRequest;
import vn.eledevo.vksbe.dto.request.computer.ComputerRequestForCreate;
import vn.eledevo.vksbe.dto.request.computer.ComputerToCheckExist;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.ResultList;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponse;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponseFilter;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.service.computer.ComputerService;

@RestController
@RequestMapping("/api/v1/private/computers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý thiết bị máy tính")
public class ComputerController {
    ComputerService computerService;

    @GetMapping("/disconnected")
    @Operation(summary = "Lấy danh sách máy tính không liên kết với tài khoản")
    public ApiResponse<ResultList<ComputerResponse>> getComputerDisconnectedList(
            @RequestParam(required = false) String textSearch) {
        return ApiResponse.ok(computerService.getDisconnectedComputers(textSearch));
    }

    @PatchMapping("/update/computer-info/{id}")
    @Operation(summary = "Chỉnh sửa thông tin máy tính")
    public ApiResponse<HashMap<String, String>> updateComputer(
            @Valid @PathVariable("id") Long id,
            @Valid @RequestBody ComputersModel computerRequest,
            BindingResult result)
            throws ApiException, ValidationException {
        if (result.hasErrors()) {
            Map<String, String> errorMessages = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            throw new ValidationException(errorMessages);
        }
        return ApiResponse.ok(computerService.updateComputer(id, computerRequest));
    }

    @PostMapping("/create/computer-info")
    @Operation(summary = "Tạo mới thiết bị máy tính")
    public ApiResponse<HashMap<String, String>> createComputer(@Valid @RequestBody ComputerRequestForCreate request)
            throws ApiException, ValidationException {
        return ApiResponse.ok(computerService.createComputer(request));
    }

    @PostMapping("")
    @Operation(summary = "Xem danh sách thiết bị máy tính")
    public ApiResponse<ResponseFilter<ComputerResponseFilter>> getComputerList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestBody ComputerRequest computerRequest)
            throws ApiException {
        return ApiResponse.ok(computerService.getComputerList(computerRequest, page, pageSize));
    }

    @PostMapping("/check-exist-computer")
    @Operation(summary = "Check thiết bị máy tính tồn tại trong hệ thống")
    public ApiResponse<HashMap<String, String>> getComputerList(@RequestBody ComputerToCheckExist computer)
            throws ApiException {
        return ApiResponse.ok(computerService.checkExistComputer(computer));
    }
}
