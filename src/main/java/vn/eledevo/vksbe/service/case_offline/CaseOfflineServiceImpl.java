package vn.eledevo.vksbe.service.case_offline;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.eledevo.vksbe.constant.ErrorCodes.CaseErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.constant.Role;
import vn.eledevo.vksbe.dto.model.document.DocumentOfflineProjection;
import vn.eledevo.vksbe.dto.response.case_flow.CaseFlowResponse;
import vn.eledevo.vksbe.dto.response.case_offline.DocumentOfflineResponse;
import vn.eledevo.vksbe.dto.response.cases.CaseInfomationResponse;
import vn.eledevo.vksbe.entity.AccountCase;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Cases;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.repository.*;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.SecurityUtils;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CaseOfflineServiceImpl implements CaseOfflineService {
    CaseRepository caseRepository;
    CitizenRepository citizenRepository;
    DocumentRepository documentRepository;
    AccountCaseRepository accountCaseRepository;
    AccountRepository accountRepository;
    CaseFlowRepository caseFlowRepository;
    HistoryService historyService;

    private CaseInfomationResponse getCaseInfo(Cases cases) {
        return CaseInfomationResponse.builder()
                .id(cases.getId())
                .name(cases.getName())
                .code(cases.getCode())
                .departmentId(cases.getDepartments().getId())
                .departmentName(cases.getDepartments().getName())
                .statusName(cases.getCase_status().getName())
                .statusId(cases.getCase_status().getId())
                .createdAt(cases.getCreatedAt().toLocalDate())
                .updatedAt(cases.getUpdatedAt().toLocalDate())
                .type(cases.getCaseType())
                .description(cases.getDescription())
                .createdBy(cases.getCreatedBy())
                .actualTime(cases.getActualTime())
                .build();
    }

    private LinkedHashMap<Long, DocumentOfflineResponse> getOfflineDocuments(
            List<DocumentOfflineProjection> offlineDocuments,
            List<DocumentOfflineProjection> defaultDocuments,
            String documentType,
            Long id) {
        LinkedHashMap<Long, DocumentOfflineResponse> result = new LinkedHashMap<>();
        LinkedHashMap<Long, DocumentOfflineResponse> defaultDocs = new LinkedHashMap<>();
        for (DocumentOfflineProjection doc : defaultDocuments) {
            if (documentType.equals(doc.getDocumentType())) {
                DocumentOfflineResponse response = new DocumentOfflineResponse();
                response.setId(doc.getId());
                response.setName(doc.getName());
                response.setUriName(doc.getUriName());
                response.setDocumentType(doc.getDocumentType());
                response.setType(doc.getType());
                response.setSize(doc.getSize());
                response.setDescription(doc.getDescription());
                response.setCreatedAt(doc.getCreatedAt());
                response.setUpdatedAt(doc.getUpdatedAt());
                response.setCreatedBy(doc.getCreatedBy());
                response.setUpdatedBy(doc.getUpdatedBy());
                response.setPath(doc.getPath());
                response.setParentId(doc.getParentId());
                response.setChildIds(documentRepository.getChildrenIds(doc.getId(), id));

                defaultDocs.put(doc.getId(), response);
            }
        }

        LinkedHashMap<Long, DocumentOfflineResponse> offlineDocs = offlineDocuments.stream()
                .filter(doc -> documentType.equalsIgnoreCase(doc.getDocumentType()))
                .collect(Collectors.toMap(
                        DocumentOfflineProjection::getId,
                        doc -> {
                            DocumentOfflineResponse response = new DocumentOfflineResponse();
                            response.setId(doc.getId());
                            response.setName(doc.getName());
                            response.setUriName(doc.getUriName());
                            response.setDocumentType(doc.getDocumentType());
                            response.setType(doc.getType());
                            response.setSize(doc.getSize());
                            response.setDescription(doc.getDescription());
                            response.setCreatedAt(doc.getCreatedAt());
                            response.setUpdatedAt(doc.getUpdatedAt());
                            response.setCreatedBy(doc.getCreatedBy());
                            response.setUpdatedBy(doc.getUpdatedBy());
                            response.setPath(doc.getPath());
                            response.setParentId(doc.getParentId());

                            String childIdsStr = doc.getChildIds();
                            List<Long> childIds = (childIdsStr != null && !childIdsStr.isEmpty())
                                    ? Arrays.stream(childIdsStr.split(","))
                                            .map(Long::valueOf)
                                            .toList()
                                    : new ArrayList<>();
                            response.setChildIds(childIds);

                            return response;
                        },
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));

        result.putAll(defaultDocs);
        result.putAll(offlineDocs);
        return result;
    }

    private HashMap<String, DocumentOfflineResponse> getDefaultDocuments(List<DocumentOfflineProjection> documents) {
        HashMap<String, DocumentOfflineResponse> defaultDocuments = new HashMap<>();

        for (DocumentOfflineProjection doc : documents) {
            DocumentOfflineResponse response = new DocumentOfflineResponse();
            response.setId(doc.getId());
            response.setName(doc.getName());
            response.setUriName(doc.getUriName());
            response.setDocumentType(doc.getDocumentType());
            response.setType(doc.getType());
            response.setSize(doc.getSize());
            response.setDescription(doc.getDescription());
            response.setCreatedAt(doc.getCreatedAt());
            response.setUpdatedAt(doc.getUpdatedAt());
            response.setCreatedBy(doc.getCreatedBy());
            response.setUpdatedBy(doc.getUpdatedBy());
            response.setPath(doc.getPath());
            response.setParentId(doc.getParentId());
            response.setChildIds(null);

            defaultDocuments.put(doc.getDocumentType(), response);
        }

        return defaultDocuments;
    }

    @Override
    public List<String> getUriName(Long caseId) throws ApiException {
        Cases cases = caseRepository.findById(caseId).orElseThrow(() -> new ApiException(CaseErrorCode.CASE_NOT_FOUND));

        checkPermissionToDownload(cases, caseId);

        List<String> mergedList = new ArrayList<>();

        // get avatar lãnh đạo phụ trách, kiểm sát viên
        List<String> avatar = accountRepository.getUriNameOfLead(caseId);

        // get ảnh điều tra viên, bị can, bị cáo
        List<String> profileImage = citizenRepository.getUriNameOfCitizens(caseId);

        // get ảnh của sơ đồ tư duy
        List<String> uriNameOfCaseFlow = caseFlowRepository.getUriNameOfCaseFlow(caseId);

        // get ảnh của tài liệu
        List<String> uriNameOfDocument = documentRepository.getUriNameOfDocuments(caseId);

        mergedList.addAll(avatar);
        mergedList.addAll(profileImage);
        mergedList.addAll(uriNameOfCaseFlow);
        mergedList.addAll(uriNameOfDocument);
        return mergedList;
    }

    private void checkPermissionToDownload(Cases cases, Long id) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        Role loginAccRole = Role.valueOf(loginAccount.getRoles().getCode());

        if (!loginAccRole.equals(Role.VIEN_TRUONG)
                && !loginAccRole.equals(Role.VIEN_PHO)
                && !cases.getDepartments()
                        .getId()
                        .equals(loginAccount.getDepartments().getId())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
        }

        if (loginAccRole.equals(Role.PHO_PHONG) || loginAccRole.equals(Role.KIEM_SAT_VIEN)) {
            AccountCase accountCase = accountCaseRepository.findByAccountId(loginAccount.getId(), id);
            if (accountCase == null) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
            } else if (Boolean.FALSE.equals(accountCase.getHasPermissionDownload())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
            }
        }
    }

    private CaseFlowResponse getCaseFlow(Long id) {
        var caseFlow = caseFlowRepository.findFirstByCases_Id(id).orElse(null);
        if (caseFlow == null) {
            return null;
        }
        Accounts accountCreate =
                accountRepository.findByUsername(caseFlow.getCreatedBy()).orElse(null);
        Accounts accountUpdate =
                accountRepository.findByUsername(caseFlow.getUpdatedBy()).orElse(null);
        return CaseFlowResponse.builder()
                .id(caseFlow.getId())
                .name(caseFlow.getName())
                .url(caseFlow.getUrl())
                .uriName(caseFlow.getUriName())
                .createdAt(caseFlow.getCreatedAt().toLocalDate())
                .createdBy((accountCreate != null ? accountCreate.getProfile().getFullName() + " - " : "")
                        + caseFlow.getCreatedBy())
                .updatedBy((accountUpdate != null ? accountUpdate.getProfile().getFullName() + " - " : "")
                        + caseFlow.getUpdatedBy())
                .build();
    }
}
