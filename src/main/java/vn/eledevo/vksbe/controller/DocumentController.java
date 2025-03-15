package vn.eledevo.vksbe.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.dto.request.document.DocumentRequest;
import vn.eledevo.vksbe.dto.response.ApiResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.document.DocumentParentResponse;
import vn.eledevo.vksbe.dto.response.document.DocumentResponse;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.service.document.DocumentService;
import vn.eledevo.vksbe.utils.minio.MinioService;

@RestController
@RequestMapping("/api/v1/private/documents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quản lý tài liệu")
public class DocumentController {
    DocumentService documentService;
    MinioService minioService;

    @PostMapping("/folder/{caseId}")
    @Operation(summary = "//")
    public ApiResponse<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentRequest documentRequest, @PathVariable(value = "caseId") Long caseId)
            throws ApiException {

        return ApiResponse.ok(documentService.createFolder(caseId, documentRequest));
    }

    @PostMapping(value = "/file/{caseId}/{parentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file")
    public ApiResponse<DocumentResponse> createFile(
            @RequestBody MultipartFile multipartFile,
            @PathVariable(value = "caseId") Long caseId,
            @PathVariable(value = "parentId") Long parentId,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileSize") String fileSize,
            @RequestParam("fileType") String fileType)
            throws Exception {

        return ApiResponse.ok(documentService.createFile(
                caseId, parentId, multipartFile, fileName, chunkNumber, totalChunks, fileSize, fileType));
    }

    @PatchMapping(value = "/{documentId}/update")
    @Operation(summary = "Cập nhật tài liệu")
    public ApiResponse<DocumentResponse> updateDocument(
            @Valid @RequestBody DocumentRequest documentRequest, @PathVariable(value = "documentId") Long documentId)
            throws ApiException {

        return ApiResponse.ok(documentService.updateDocument(documentRequest, documentId));
    }

    @GetMapping(value = "cases/{id}/documents")
    @Operation(summary = "Lấy danh sách tài liệu")
    public ApiResponse<ResponseFilter<DocumentResponse>> getDocument(
            @PathVariable(value = "id") Long id,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortType,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "0") Long documentId,
            @RequestParam(required = false, defaultValue = "1") Long page,
            @RequestParam(required = false, defaultValue = "5") Long pageSize)
            throws ApiException {

        return ApiResponse.ok(
                documentService.getDocument(id, sortType, sortField, textSearch, type, documentId, page, pageSize));
    }

    @GetMapping(value = "cases/{id}/documents/{documentsId}/bread-crumb")
    @Operation(summary = "Bread Crumb")
    public ApiResponse<List<DocumentParentResponse>> getBreadCrumb(
            @PathVariable(value = "id") Long id,
            @PathVariable(value = "documentsId") Long documentsId,
            @RequestParam(required = false) String type)
            throws ApiException {

        return ApiResponse.ok(documentService.getBreadCrumb(id, documentsId, type));
    }
}
