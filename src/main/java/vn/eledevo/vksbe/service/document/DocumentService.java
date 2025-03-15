package vn.eledevo.vksbe.service.document;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import vn.eledevo.vksbe.dto.request.document.DocumentRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.document.DocumentParentResponse;
import vn.eledevo.vksbe.dto.response.document.DocumentResponse;
import vn.eledevo.vksbe.exception.ApiException;

public interface DocumentService {
    DocumentResponse createFolder(Long caseId, DocumentRequest documentRequest) throws ApiException;

    DocumentResponse createFile(
            Long caseId,
            Long parentId,
            MultipartFile file,
            String fileName,
            int chunkNumber,
            int totalChunks,
            String fileSize,
            String fileType)
            throws Exception;

    DocumentResponse updateDocument(DocumentRequest documentRequest, Long documentId) throws ApiException;

    ResponseFilter<DocumentResponse> getDocument(
            Long id,
            String sortType,
            String sortField,
            String textSearch,
            String type,
            Long documentId,
            Long page,
            Long pageSize)
            throws ApiException;

    List<DocumentParentResponse> getBreadCrumb(Long id, Long documentsId, String type) throws ApiException;
}
