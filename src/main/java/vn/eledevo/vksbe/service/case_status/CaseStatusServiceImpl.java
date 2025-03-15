package vn.eledevo.vksbe.service.case_status;

import static vn.eledevo.vksbe.constant.ActionContent.*;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.ActionContent;
import vn.eledevo.vksbe.constant.ErrorCodes.CaseStatusErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.constant.IconType;
import vn.eledevo.vksbe.constant.ObjectTableType;
import vn.eledevo.vksbe.dto.request.case_status.CaseStatusCreateRequest;
import vn.eledevo.vksbe.dto.request.case_status.CaseStatusGetRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.case_status.CaseStatusResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.CaseStatus;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.repository.CaseRepository;
import vn.eledevo.vksbe.repository.CaseStatusRepository;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.SecurityUtils;
import vn.eledevo.vksbe.utils.TimeUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CaseStatusServiceImpl implements CaseStatusService {
    CaseStatusRepository caseStatusRepository;
    CaseRepository caseRepository;
    HistoryService historyService;

    @Override
    public ResponseFilter<CaseStatusResponse> getCaseStatus(
            CaseStatusGetRequest caseStatusGetRequest, Integer page, Integer pageSize) throws ApiException {
        if (page < 1 || pageSize < 1) {
            throw new ApiException(SystemErrorCode.INVALID_PAGE_REQUEST);
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").ascending());
        Page<CaseStatusResponse> caseStatusResponsePage = caseStatusRepository.getCaseStatus(
                caseStatusGetRequest.getName(),
                TimeUtils.toLocalDateTimeStart(caseStatusGetRequest.getFromDate()),
                TimeUtils.toLocalDateTimeEnd(caseStatusGetRequest.getToDate()),
                pageable);
        //        historyService.SaveHistory(SecurityUtils.getUser(), GET_CASE_STATUS, ObjectTableType.CASE_STATUS,null,
        // CASE_STATUS, IconType.CASE.name(),null);
        return new ResponseFilter<>(
                caseStatusResponsePage.getContent(),
                (int) caseStatusResponsePage.getTotalElements(),
                caseStatusResponsePage.getSize(),
                caseStatusResponsePage.getNumber(),
                caseStatusResponsePage.getTotalPages());
    }

    @Override
    public HashMap<String, String> createCaseStatus(CaseStatusCreateRequest caseStatusCreateRequest)
            throws ApiException, ValidationException {
        if (caseStatusRepository.existsByName(caseStatusCreateRequest.getName())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("name", CaseStatusErrorCode.CASE_STATUS_NAME_ALREADY_EXIST.getMessage());
            throw new ValidationException(errors);
        }
        CaseStatus caseStatus = CaseStatus.builder()
                .name(caseStatusCreateRequest.getName())
                .description(caseStatusCreateRequest.getDescription())
                .isDefault(false)
                .build();
        caseStatus = caseStatusRepository.save(caseStatus);
        historyService.SaveHistory(
                SecurityUtils.getUser(),
                ActionContent.CREATE_CASE_STATUS,
                ObjectTableType.CASE_STATUS,
                caseStatus.getId(),
                caseStatus.getName(),
                IconType.CASE.name(),
                null);
        return new HashMap<>();
    }

    @Override
    public HashMap<String, String> updateCaseStatus(Long id, CaseStatusCreateRequest caseStatusCreateRequest)
            throws ApiException, ValidationException {
        Optional<CaseStatus> caseStatusOptional = caseStatusRepository.findById(id);
        Accounts accounts = SecurityUtils.getUser();
        if (caseStatusOptional.isEmpty()) {
            throw new ApiException(CaseStatusErrorCode.CASE_STATUS_NOT_FOUND);
        }
        if (!caseStatusCreateRequest.getName().equals(caseStatusOptional.get().getName())
                && caseStatusRepository.existsByNameAndIdNot(caseStatusCreateRequest.getName(), id)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("name", CaseStatusErrorCode.CASE_STATUS_NAME_ALREADY_EXIST.getMessage());
            throw new ValidationException(errors);
        }
        CaseStatus caseStatus = caseStatusOptional.get();
        caseStatus.setName(caseStatusCreateRequest.getName());
        if (Objects.nonNull(caseStatusCreateRequest.getDescription())) {
            caseStatus.setDescription(caseStatusCreateRequest.getDescription());
        }

        caseStatusRepository.save(caseStatus);
        historyService.SaveHistory(
                accounts,
                UPDATE_CASE_STATUS,
                ObjectTableType.CASE_STATUS,
                id,
                caseStatus.getName(),
                IconType.CASE.name(),
                null);
        return new HashMap<>();
    }

    @Override
    public CaseStatusResponse getCaseStatusDetail(Long caseStatusId) throws ApiException {
        CaseStatus existingCaseStatus = caseStatusRepository
                .findById(caseStatusId)
                .orElseThrow(() -> new ApiException(CaseStatusErrorCode.CASE_STATUS_NOT_FOUND));
        historyService.SaveHistory(
                SecurityUtils.getUser(),
                GET_CASE_STATUS_DETAIL,
                ObjectTableType.CASE_STATUS,
                caseStatusId,
                existingCaseStatus.getName(),
                IconType.CASE.name(),
                null);
        return CaseStatusResponse.builder()
                .id(existingCaseStatus.getId())
                .name(existingCaseStatus.getName())
                .description(existingCaseStatus.getDescription())
                .build();
    }

    private CaseStatus getCaseStatusById(Long id) throws ApiException {
        return caseStatusRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(CaseStatusErrorCode.CASE_STATUS_NOT_FOUND));
    }
}
