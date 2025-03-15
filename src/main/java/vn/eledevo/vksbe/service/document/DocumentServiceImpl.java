package vn.eledevo.vksbe.service.document;

import static vn.eledevo.vksbe.constant.ErrorCodes.CaseErrorCode.CASE_NOT_FOUND;
import static vn.eledevo.vksbe.constant.ErrorCodes.DocumentErrorCode.*;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.*;
import vn.eledevo.vksbe.constant.ErrorCodes.DocumentErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.dto.request.document.DocumentRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.document.DocumentParentResponse;
import vn.eledevo.vksbe.dto.response.document.DocumentResponse;
import vn.eledevo.vksbe.entity.AccountCase;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Cases;
import vn.eledevo.vksbe.entity.Documents;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.mapper.DocumentMapper;
import vn.eledevo.vksbe.repository.AccountCaseRepository;
import vn.eledevo.vksbe.repository.CaseRepository;
import vn.eledevo.vksbe.repository.DocumentRepository;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.FileUtils;
import vn.eledevo.vksbe.utils.SecurityUtils;
import vn.eledevo.vksbe.utils.minio.MinioService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentServiceImpl implements DocumentService {
    DocumentRepository documentRepository;
    CaseRepository caseRepository;
    AccountCaseRepository accountCaseRepository;
    MinioService minioService;
    HistoryService historyService;

    @Override
    @Transactional
    public DocumentResponse createFolder(Long caseId, DocumentRequest documentRequest) throws ApiException {
        Cases cases = caseRepository.findById(caseId).orElseThrow(() -> new ApiException(CASE_NOT_FOUND));
        Accounts accounts = SecurityUtils.getUser();
        checkRoleEditPersons(accounts, cases);
        Documents documentsParent = null;
        if (documentRequest.getParentId() != null) {
            documentsParent = documentRepository
                    .findById(documentRequest.getParentId())
                    .orElseThrow(() -> new ApiException(PARENT_DOC_NOT_FOUND));
        }
        Documents documentsEntity = Documents.builder()
                .name(documentRequest.getName())
                .documentType(documentsParent != null ? documentsParent.getDocumentType() : null)
                .cases(cases)
                .type(DocumentType.FOLDER.name())
                .parentId(documentsParent)
                .isDefault(false)
                .build();
        documentRepository.save(documentsEntity);

        // Todo: Xử lí thêm lịch sử
        historyService.SaveHistory(
                accounts,
                "Tạo mới thư mục",
                ObjectTableType.DOCUMENT,
                documentsEntity.getId(),
                documentsEntity.getName(),
                IconType.FOLDER.name(),
                cases.getId());
        return null;
    }

    @Override
    @Transactional
    public DocumentResponse createFile(
            Long caseId,
            Long parentId,
            MultipartFile file,
            String fileName,
            int chunkNumber,
            int totalChunks,
            String fileSize,
            String fileType)
            throws Exception {
        Cases cases = caseRepository.findById(caseId).orElseThrow(() -> new ApiException(CASE_NOT_FOUND));
        if (file == null) {
            throw new ApiException(FILE_CAN_NOT_EMPTY);
        }
        Accounts accounts = SecurityUtils.getUser();
        checkRoleEditPersons(accounts, cases);

        Documents documentsParent = null;
        if (parentId != null) {
            documentsParent =
                    documentRepository.findById(parentId).orElseThrow(() -> new ApiException(PARENT_DOC_NOT_FOUND));
        }
        if (!FileUtils.validateExtensionFileDocumentChunk(fileName)) {
            throw new ApiException(E_UNSUPPORT_TYPE_FILE);
        }

        String fileTypes = FileUtils.getFileTypeChunk(fileName);
        //        String uniqueName = UUID.randomUUID() + "_" + fileName;
        Map<String, Object> fileDocument = minioService.handleChunkUpload(fileName, chunkNumber, totalChunks, file);
        Boolean successMerge = (Boolean) fileDocument.getOrDefault("success_merge", false);
        if (Boolean.TRUE.equals(successMerge)) {
            String path = (String) fileDocument.get("urlFile");
            if (parentId != null) {
                Documents documentsEntity = Documents.builder()
                        .name(fileName)
                        .path(path)
                        .size(Long.parseLong(fileSize))
                        .documentType(documentsParent.getDocumentType())
                        .cases(cases)
                        .type(fileTypes)
                        .parentId(documentsParent)
                        .isDefault(false)
                        .uriName(FileUtils.getUriName(path))
                        .build();
                documentRepository.save(documentsEntity);
                historyService.SaveHistory(
                        accounts,
                        "Tạo mới tài liệu",
                        ObjectTableType.DOCUMENT,
                        documentsEntity.getId(),
                        documentsEntity.getName(),
                        FileUtils.getFileType(file),
                        cases.getId());
            }
        }
        return null;
    }

    @Override
    public DocumentResponse updateDocument(DocumentRequest documentRequest, Long documentId) throws ApiException {
        Documents document = documentRepository.findById(documentId).orElseThrow(() -> new ApiException(DOC_NOT_FOUND));
        if (document.getIsDefault()) {
            throw new ApiException(DocumentErrorCode.CAN_NOT_UPDATE_DEFAULT);
        }
        Accounts accounts = SecurityUtils.getUser();
        checkRoleEditPersons(accounts, document.getCases());
        document.setName(documentRequest.getName());
        documentRepository.save(document);
        historyService.SaveHistory(
                accounts,
                "Chỉnh sửa tên tài liệu "
                        + (document.getType().equals(DocumentType.FOLDER.name()) ? "thư mục" : "tài liệu"),
                ObjectTableType.DOCUMENT,
                document.getId(),
                document.getName(),
                document.getType(),
                document.getCases().getId());
        return null;
    }

    @Override
    public ResponseFilter<DocumentResponse> getDocument(
            Long caseId,
            String sortType,
            String sortField,
            String textSearch,
            String type,
            Long documentId,
            Long page,
            Long pageSize)
            throws ApiException {
        // Kiểm tra loại tài liệu hợp lệ
        Set<String> validTypes = Set.of(
                DocumentType.INVESTIGATION.name(),
                DocumentType.TRASH.name(),
                DocumentType.TRIAL.name(),
                DocumentType.ROOT.name());
        if (!validTypes.contains(type)) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }

        // Lấy thông tin vụ án và tài khoản
        Cases cases = caseRepository.findById(caseId).orElseThrow(() -> new ApiException(CASE_NOT_FOUND));
        Accounts accounts = SecurityUtils.getUser();
        checkRoleEditPersons(accounts, cases);

        // Kiểm tra tham số phân trang
        if (page < 1 || pageSize < 1) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }

        // Xử lý tài liệu loại ROOT riêng
        if (type.equals(DocumentType.ROOT.name())) {
            Page<Documents> documentsList = documentRepository.findByIsDefault(true, PageRequest.of(0, 5));
            var listRes = new ArrayList<>(documentsList.getContent());
            listRes.removeIf(f -> f.getDocumentType().equals(DocumentType.TRASH.name()));
            return new ResponseFilter<>(
                    listRes.stream().map(DocumentMapper::toResponse).collect(Collectors.toList()),
                    (int) documentsList.getTotalElements(),
                    documentsList.getSize(),
                    documentsList.getNumber(),
                    documentsList.getTotalPages());
        }

        // Thiết lập sắp xếp
        Sort.Order defaultOrder = Sort.Order.asc("path");
        Sort.Order dynamicOrder = new Sort.Order(
                "desc".equalsIgnoreCase(sortType) ? Sort.Direction.DESC : Sort.Direction.ASC,
                (sortField != null && !sortField.isBlank()) ? sortField : "updatedAt");
        Sort sort = Sort.by(defaultOrder).and(Sort.by(dynamicOrder));

        // Thiết lập Pageable (trang bắt đầu từ 0)
        Pageable pageable = PageRequest.of(page.intValue() - 1, pageSize.intValue(), sort);

        // Xử lý documentId và textSearch
        if (documentId == 0) {
            documentId = documentRepository
                    .findByDocDefault(type)
                    .orElseThrow(() -> new ApiException(DOC_NOT_FOUND))
                    .getId();
        }
        if (textSearch != null && !textSearch.isBlank()) {
            textSearch = textSearch.toLowerCase();
            documentId = null; // Đặt lại documentId khi có tìm kiếm văn bản
        }

        // Lấy danh sách tài liệu với phân trang và sắp xếp
        Page<DocumentResponse> documentsList =
                documentRepository.getDocument(caseId, textSearch, type, documentId, pageable);
        List<DocumentResponse> listRes = new ArrayList<>(documentsList.getContent());
        if (!CollectionUtils.isEmpty(documentsList.getContent()) && sortType != null && sortField != null) {
            List<DocumentResponse> listFile = documentsList.getContent().stream()
                    .filter(f -> !f.getType().equals(DocumentType.FOLDER.name()))
                    .collect(Collectors.toList());

            listRes.removeAll(listFile);
            Comparator<DocumentResponse> comparator = sortField.equalsIgnoreCase("updatedAt")
                    ? Comparator.comparing(DocumentResponse::getUpdatedAt)
                    : Comparator.comparing(DocumentResponse::getName);

            if (sortType.equalsIgnoreCase("desc")) {
                comparator = comparator.reversed();
            }
            listFile.sort(comparator);
            listRes.addAll(listFile);
        }
        return new ResponseFilter<>(
                listRes,
                (int) documentsList.getTotalElements(),
                documentsList.getSize(),
                documentsList.getNumber(),
                documentsList.getTotalPages());
    }

    @Override
    public List<DocumentParentResponse> getBreadCrumb(Long id, Long documentsId, String type) throws ApiException {
        // todo: thiếu validate document gửi xuống ko nằm trong cases gửi xuống
        Set<String> validTypes = Set.of(
                DocumentType.INVESTIGATION.name(),
                DocumentType.TRASH.name(),
                DocumentType.TRIAL.name(),
                DocumentType.ROOT.name());
        if (!validTypes.contains(type)) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }

        Documents documentPresent = documentRepository.findById(documentsId).orElse(null);

        // Kiểm tra điều kiện và ném lỗi nếu không thỏa mãn
        if (documentPresent != null && !documentPresent.getIsDefault()) {
            if (!Objects.equals(documentPresent.getCases().getId(), id)) {
                throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
            }
        }

        List<DocumentParentResponse> breadCrumb = new ArrayList<>();
        breadCrumb.add(new DocumentParentResponse(0L, DocumentType.ROOT.name(), DocumentType.ROOT.name()));

        // Nếu type là ROOT, trả về breadcrumb chứa ROOT
        if (type.equals(DocumentType.ROOT.name())) {
            return breadCrumb;
        }

        // Lấy document mặc định theo loại (type) nếu tồn tại, hoặc ném lỗi nếu không tìm thấy
        DocumentParentResponse defaultDoc =
                documentRepository.findByDocDefault(type).orElseThrow(() -> new ApiException(PARENT_DOC_NOT_FOUND));
        breadCrumb.add(defaultDoc);

        // Nếu documentPresent khác null, lấy toàn bộ parent hierarchy một lần
        if (documentPresent != null) {
            List<Documents> parentHierarchy = getParentHierarchy(documentPresent);
            int position = 2;

            for (Documents doc : parentHierarchy) {
                if (!doc.getIsDefault()) {
                    breadCrumb.add(
                            position++, new DocumentParentResponse(doc.getId(), doc.getName(), doc.getDocumentType()));
                }
                if (position == 5) { // Giới hạn độ sâu của breadcrumb
                    break;
                }
            }
        }

        return breadCrumb;
    }

    private List<Documents> getParentHierarchy(Documents document) {
        List<Documents> hierarchy = new ArrayList<>();
        hierarchy.add(document);
        int i = 0;
        Documents current = document.getParentId();

        // Lặp qua các parent documents cho đến khi null
        while (current != null) {
            hierarchy.add(current);
            current = current.getParentId();
            i++;
            if (i > 1) {
                Collections.reverse(hierarchy);
                return hierarchy;
            }
        }

        // Đảo ngược thứ tự hierarchy để đảm bảo parent nằm trước con của nó trong danh sách
        Collections.reverse(hierarchy);
        return hierarchy;
    }

    private void updateTypeChildrenDocument(String type, List<Documents> documents, Cases cases) {
        documents.forEach(document -> {
            document.setDocumentType(type);
        });
        documentRepository.saveAll(documents); // Batch save

        documents.forEach(document -> {
            List<Documents> documentsChildren = documentRepository.findByParentId(document);
            if (!CollectionUtils.isEmpty(documentsChildren)) {
                updateTypeChildrenDocument(type, documentsChildren, cases);
            }
        });
    }

    private void checkRoleEditPersons(Accounts accounts, Cases cases) throws ApiException {
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())) return;
        if (!accounts.getDepartments().getId().equals(cases.getDepartments().getId())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
        }
        if (accounts.getRoles().getCode().equals(Role.PHO_PHONG.name())
                || accounts.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name())) {
            Optional<AccountCase> accountCases =
                    accountCaseRepository.getAccountAccessTrue(accounts.getId(), cases.getId());
            if (accountCases.isEmpty()) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
            }
        }
    }

    private void moveItemAndChildrenToTrash(Documents item, List<Documents> documentsToUpdate, DocumentType type) {

        // Nếu là thư mục, đệ quy xử lý tất cả tài liệu con
        if (item.getType().equals(DocumentType.FOLDER.name())) {
            List<Documents> children = documentRepository.findByParentId(item);
            for (Documents child : children) {
                moveItemAndChildrenToTrash(child, documentsToUpdate, type);
            }
        }

        // Cập nhật loại tài liệu thành TRASH và thêm vào danh sách để lưu
        item.setDocumentType(type.name());
        documentsToUpdate.add(item);
    }
}
