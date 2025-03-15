package vn.eledevo.vksbe.controller;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.request.history.HistoryFilterRequest;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.history.HistoryResponse;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.service.histories.HistoryService;

@RestController
@RequestMapping("/api/v1/private/histories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý lịch sử hệ thống ")
public class HistoryController {
    HistoryService historyService;

    @PostMapping("")
    @Operation(summary = "Xem lịch sử thay đổi hệ thống")
    public ApiResponse<ResponseFilter<HistoryResponse>> getHistoryCase(
            @RequestParam Long page, @RequestParam Long pageSize, @RequestBody HistoryFilterRequest request)
            throws ApiException {
        return ApiResponse.ok(historyService.getHistoryApp(request, page, pageSize));
    }
}
