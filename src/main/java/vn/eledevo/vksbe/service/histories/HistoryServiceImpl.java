package vn.eledevo.vksbe.service.histories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.*;
import vn.eledevo.vksbe.constant.ErrorCodes.CaseErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.dto.request.history.HistoryFilterRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.history.HistoryResponse;
import vn.eledevo.vksbe.entity.AccountCase;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Cases;
import vn.eledevo.vksbe.entity.Histories;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.repository.AccountCaseRepository;
import vn.eledevo.vksbe.repository.CaseRepository;
import vn.eledevo.vksbe.repository.HistoryRepository;
import vn.eledevo.vksbe.utils.SecurityUtils;
import vn.eledevo.vksbe.utils.TimeUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HistoryServiceImpl implements HistoryService {
    HistoryRepository historyRepository;
    AccountCaseRepository accountCaseRepository;
    CaseRepository caseRepository;

    @Override
    public void SaveHistory(
            Accounts accounts,
            String action,
            ObjectTableType objectTableType,
            Long objectId,
            String objectName,
            String iconType,
            Long caseId) {
        Histories histories = Histories.builder()
                .staffId(accounts.getId())
                .staffCode(accounts.getUsername())
                .fullName(accounts.getProfile().getFullName())
                .action(action)
                .objectType(objectTableType.name())
                .objectId(objectId)
                .objectName(objectName)
                .iconType(iconType)
                .caseId(caseId)
                .build();
        historyRepository.save(histories);
    }

    @Override
    public ResponseFilter<HistoryResponse> getHistoryCase(
            HistoryFilterRequest request, Long caseId, Long page, Long pageSize) throws ApiException {
        Cases cases = getCaseById(caseId);
        Accounts accountLogin = SecurityUtils.getUser();
        checkRoleViewHistory(accountLogin, cases);

        // Kiểm tra giá trị của page và pageSize
        if (page < 1 || pageSize < 1) {
            throw new ApiException(SystemErrorCode.INVALID_PAGE_REQUEST);
        }

        String textSearch = Optional.ofNullable(request)
                .map(HistoryFilterRequest::getTextSearch)
                .filter(s -> !s.isBlank())
                .orElse(null);

        LocalDate formDate = Optional.ofNullable(request)
                .map(HistoryFilterRequest::getFromDate)
                .orElse(null);

        LocalDate toDate = Optional.ofNullable(request)
                .map(HistoryFilterRequest::getToDate)
                .orElse(null);

        Pageable pageable = PageRequest.of(
                (page).intValue() - 1, pageSize.intValue(), Sort.by("timestamp").descending());

        Page<HistoryResponse> historyResponsePage = historyRepository.searchHistoryCase(
                textSearch,
                caseId,
                TimeUtils.toLocalDateTimeStart(formDate),
                TimeUtils.toLocalDateTimeEnd(toDate),
                pageable);

        return new ResponseFilter<>(
                historyResponsePage.getContent(),
                (int) historyResponsePage.getTotalElements(),
                historyResponsePage.getSize(),
                historyResponsePage.getNumber(),
                historyResponsePage.getTotalPages());
    }

    private void checkRoleViewHistory(Accounts accounts, Cases cases) throws ApiException {
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

    private Cases getCaseById(Long id) throws ApiException {
        return caseRepository.findById(id).orElseThrow(() -> new ApiException(CaseErrorCode.CASE_NOT_FOUND));
    }

    @Override
    public ResponseFilter<HistoryResponse> getHistoryApp(HistoryFilterRequest request, Long page, Long pageSize)
            throws ApiException {
        if (page < 1 || pageSize < 1) {
            throw new ApiException(SystemErrorCode.INVALID_PAGE_REQUEST);
        }

        String textSearch = Optional.ofNullable(request)
                .map(HistoryFilterRequest::getTextSearch)
                .filter(s -> !s.isBlank())
                .orElse(null);

        LocalDate formDate = Optional.ofNullable(request)
                .map(HistoryFilterRequest::getFromDate)
                .orElse(null);

        LocalDate toDate = Optional.ofNullable(request)
                .map(HistoryFilterRequest::getToDate)
                .orElse(null);

        Pageable pageable = PageRequest.of(
                (page).intValue() - 1, pageSize.intValue(), Sort.by("timestamp").descending());

        Page<HistoryResponse> historyResponsePage = historyRepository.searchHistoryApp(
                textSearch, TimeUtils.toLocalDateTimeStart(formDate), TimeUtils.toLocalDateTimeEnd(toDate), pageable);

        return new ResponseFilter<>(
                historyResponsePage.getContent(),
                (int) historyResponsePage.getTotalElements(),
                historyResponsePage.getSize(),
                historyResponsePage.getNumber(),
                historyResponsePage.getTotalPages());
    }
}
