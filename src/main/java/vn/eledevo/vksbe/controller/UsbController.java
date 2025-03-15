package vn.eledevo.vksbe.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import jakarta.validation.Valid;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.request.UsbRequest;
import vn.eledevo.vksbe.dto.request.usb.UsbToken;
import vn.eledevo.vksbe.dto.request.usb.UsbTokenInfo;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.usb.UsbResponseFilter;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.service.usb.UsbService;

@RestController
@RequestMapping("/api/v1/private/usbs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý thiết bị USB")
public class UsbController {

    UsbService usbService;

    @PostMapping("")
    @Operation(summary = "Xem danh sách thiết bị USB")
    public ApiResponse<ResponseFilter<UsbResponseFilter>> getUsbDeviceList(
            @RequestBody UsbRequest usbRequest,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize)
            throws ApiException {
        return ApiResponse.ok(usbService.getUsbByFilter(usbRequest, page, pageSize));
    }

    @PostMapping("/download/{idAccount}")
    @Operation(summary = "Tạo USB token liên kết với tài khoản")
    public ResponseEntity<InputStreamResource> createUsbToken(
            @PathVariable Long idAccount, @RequestBody UsbToken usbToken) throws Exception {
        String zipFilePath = usbService.createUsbToken(idAccount, usbToken);

        // Trả về file ZIP cho frontend
        File zipFile = new File(zipFilePath);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=app_usb.zip");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo USB token")
    public ApiResponse<HashMap<String, String>> creatUsb(@RequestBody @Valid UsbTokenInfo usbTokenInfo)
            throws ValidationException {
        return ApiResponse.ok(usbService.createUsb(usbTokenInfo));
    }
}
