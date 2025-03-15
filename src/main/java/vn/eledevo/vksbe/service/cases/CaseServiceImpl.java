package vn.eledevo.vksbe.service.cases;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.jsonwebtoken.lang.Collections;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.eledevo.vksbe.constant.*;
import vn.eledevo.vksbe.constant.ErrorCodes.*;
import vn.eledevo.vksbe.dto.model.account.AccountDownloadResponse;
import vn.eledevo.vksbe.dto.request.cases.*;
import vn.eledevo.vksbe.dto.response.*;
import vn.eledevo.vksbe.dto.response.account.StakeHolderResponse;
import vn.eledevo.vksbe.dto.response.account_case.AccountDownloadCaseResponse;
import vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse;
import vn.eledevo.vksbe.entity.*;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.mapper.AccountMapper;
import vn.eledevo.vksbe.repository.*;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.SecurityUtils;
import vn.eledevo.vksbe.utils.minio.MinioService;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {
    RoleRepository roleRepository;
    CaseRepository caseRepository;
    CitizenRepository citizenRepository;
    AccountCaseRepository accountCaseRepository;
    CaseStatusRepository caseStatusRepository;
    AccountRepository accountRepository;
    DepartmentRepository departmentRepository;
    DocumentRepository documentRepository;
    CasePersonRepository casePersonRepository;
    HistoryService historyService;
    MinioService minioService;

    MindmapTemplateRepository mindmapTemplateRepository;

    private Cases getCaseById(Long id) throws ApiException {
        return caseRepository.findById(id).orElseThrow(() -> new ApiException(CaseErrorCode.CASE_NOT_FOUND));
    }

    private boolean isNotReadDataDepartment(Accounts accounts, Cases cases) {
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())) {
            return false;
        }

        return !accounts.getDepartments().getId().equals(cases.getDepartments().getId());
    }
    //    Chau code create
    private Long getCaseId(CaseCreateRequest caseCreateRequest){
        Cases cases = new Cases();
        Long loginAccount = SecurityUtils.getDepartmentId();
        Optional<Departments> departments = departmentRepository.findById(caseCreateRequest.getDepartmentId());
        if (accountRepository.getIsCreateCase(loginAccount)) {
            if (caseRepository.existsByName(caseCreateRequest.getName())) {
                return null;
            }
            cases.setName(caseCreateRequest.getName());
            cases.setDescription(caseCreateRequest.getDescription());
            cases.setCode(caseCreateRequest.getCode());
            cases.setCase_status(caseStatusRepository.findById(1L).get());
            cases.setCaseType(caseCreateRequest.getType().name());
            System.out.println(departments.get().getId());
            if (departments.get().getName().equals(caseCreateRequest.getDepartmentName())) {
                cases.setDepartments(departments.get());
                caseRepository.save(cases);
            }
            return cases.getId();
        }
        return null;
    }

    @Override
    public Long createCase(CaseCreateRequest caseCreateRequest) {
        Optional<Departments> departments = departmentRepository.findById(caseCreateRequest.getDepartmentId());
        Long loginAccount = SecurityUtils.getDepartmentId();
        if (loginAccount.equals(1L) || loginAccount.equals(departments.get().getId())) {
                return getCaseId(caseCreateRequest);
        }
        else if (roleRepository.getRole(SecurityUtils.getRoleId()).equals(Role.PHO_PHONG.name()) || roleRepository.getRole(SecurityUtils.getRoleId()).equals(Role.KIEM_SAT_VIEN.name())) {
                return getCaseId(caseCreateRequest);
        }
        return null;
    }

    private ResponseFilter<CitizenCaseResponse> genderResponseFilterCitizenCaseResponse(
            Long id, String textSearch, int page, int pageSize, List<String> types) throws ApiException {
        Cases cases = getCaseById(id);
        Accounts loginAccount = SecurityUtils.getUser();
        validateAccountPermissionCase(loginAccount, cases);
        if (page < 1 || pageSize < 1) {
            throw new ApiException(SystemErrorCode.INVALID_PAGE_REQUEST);
        }

        Pageable pageable =
                PageRequest.of(page - 1, pageSize, Sort.by("updatedAt").descending());

        Page<CitizenCaseResponse> pages =
                citizenRepository.searchAllInvestigatorByCaseId(cases.getId(), textSearch, types, pageable);

        //        historyService.SaveHistory(
        //                loginAccount,
        //                ActionContent.GET_INVESTIGATOR_OR_SUSPECT_DEFENDANT_IN_CASE,
        //                ObjectTableType.CASE,
        //                cases.getId(),
        //                cases.getName(),
        //                IconType.CASE.name(),
        //                cases.getId()
        //        );

        return new ResponseFilter<>(
                pages.getContent(), (int) pages.getTotalElements(), pages.getSize(), page, pages.getTotalPages());
    }

    //    Chau code get all stake holder
    @Override
    public ResponseFilter<Page> getAllStakeHolderByCaseId(Long id, int page, int pageSize)
            throws ApiException {
        return responseStakeHolder(id, page, pageSize);
    }

    private ResponseFilter<Page> responseStakeHolder(Long id, int page, int pageSize)
            throws ApiException {
        Cases cases = getCaseById(id);
        Accounts loginAccount = SecurityUtils.getUser();
        validateAccountPermissionCase(loginAccount, cases);
        if (page < 1 || pageSize < 1) {
            throw new ApiException(SystemErrorCode.INVALID_PAGE_REQUEST);
        }
        Pageable pageable = Pageable.ofSize(pageSize);
        Page list = accountRepository.getStakeHolderById(cases.getDepartments().getId(), pageable);

        Page pages = new Page() {
            @Override
            public int getTotalPages() {
                return page;
            }

            @Override
            public long getTotalElements() {
                return list.getContent().size();
            }

            @Override
            public Page map(Function converter) {
                return null;
            }

            @Override
            public int getNumber() {
                return 0;
            }

            @Override
            public int getSize() {
                return list.getContent().size();
            }

            @Override
            public int getNumberOfElements() {
                return list.getContent().size();
            }

            @Override
            public List getContent() {
                return list.getContent();
            }

            @Override
            public boolean hasContent() {
                return false;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @NotNull
            @Override
            public Iterator iterator() {
                return null;
            }
        };
        return new ResponseFilter<>(
                pages.getContent(), (int) pages.getTotalElements(), pages.getSize(), page, pages.getTotalPages());
    }

    @Override
    public ResponseFilter<CitizenCaseResponse> getAllInvestigatorByCaseId(
            Long id, String textSearch, int page, int pageSize) throws ApiException {
        List<String> types = List.of(CasePersonType.INVESTIGATOR.name());
        return genderResponseFilterCitizenCaseResponse(id, textSearch, page, pageSize, types);
    }

    @Override
    public ResponseFilter<CitizenCaseResponse> getAllSuspectDefendantByCaseId(
            Long id, String textSearch, int page, int pageSize) throws ApiException {
        //        Accounts accounts = SecurityUtils.getUser();
        List<String> types = List.of(CasePersonType.SUSPECT.name(), CasePersonType.DEFENDANT.name());
        return genderResponseFilterCitizenCaseResponse(id, textSearch, page, pageSize, types);
    }

    private boolean checkIsDownload(Accounts accounts, Cases cases) {
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())
                || accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name())) {
            return true;
        }
        AccountCase accountCase = accountCaseRepository
                .findFirstAccountCaseByAccounts_IdAndCases_Id(accounts.getId(), cases.getId())
                .orElse(null);
        return accountCase != null ? accountCase.getHasPermissionDownload() : false;
    }

    private AccountCase getAccountCaseByAccountIdAndCaseId(Long accountId, Long caseId) throws ApiException {
        return accountCaseRepository
                .findFirstAccountCaseByAccounts_IdAndCases_Id(accountId, caseId)
                .orElseThrow(() -> new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION));
    }

    private AccountCase getAccountCaseByAccountIdAndCaseIdCase(Long accountId, Long caseId) throws ApiException {
        return accountCaseRepository
                .findFirstAccountCaseByAccounts_IdAndCases_Id(accountId, caseId)
                .orElseThrow(() -> new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE));
    }

    private CaseStatus getCaseStatusById(Long id) throws ApiException {
        return caseStatusRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(CaseStatusErrorCode.CASE_STATUS_NOT_FOUND));
    }

    private boolean isUserAuthorized(Accounts accounts, Cases cases) {
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())) {
            return true;
        }
        if (!accounts.getDepartments().getId().equals(cases.getDepartments().getId())) {
            return false;
        }

        return accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name());
    }

    private boolean isReadDataAccountCase(AccountCase accountCase) {
        Accounts accounts = accountCase.getAccounts();

        Cases cases = accountCase.getCases();
        if (!accounts.getDepartments().getId().equals(cases.getDepartments().getId())) {
            return false;
        }

        return Boolean.TRUE.equals(accountCase.getHasAccess());
    }

    private void validateAccountPermission(Accounts accounts, Cases cases) throws ApiException {
        if (!isUserAuthorized(accounts, cases)) {
            AccountCase accountCase = getAccountCaseByAccountIdAndCaseId(accounts.getId(), cases.getId());
            if (!isReadDataAccountCase(accountCase)) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
            }
        }
    }

    private void validateAccountPermissionCase(Accounts accounts, Cases cases) throws ApiException {
        if (!isUserAuthorized(accounts, cases)) {
            AccountCase accountCase = getAccountCaseByAccountIdAndCaseIdCase(accounts.getId(), cases.getId());
            if (!isReadDataAccountCase(accountCase)) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
            }
        }
    }

    private void throwSystemError(String key, String errorMessage) throws ValidationException {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put(key, errorMessage);

        throw new ValidationException(errorDetails);
    }

    private void updateCaseName(CaseUpdateRequest request, Cases cases) throws ValidationException {
        if (request.getName() != null && !request.getName().equals(cases.getName())) {
            if (caseRepository.existsByName(request.getName())) {
                throwSystemError("name", CaseErrorCode.CASE_EXISTED.getMessage());
            }
            cases.setName(request.getName());
        }
    }

    private void updateCaseCode(CaseUpdateRequest request, Cases cases) throws ValidationException {
        if (request.getCode() != null && !request.getCode().equals(cases.getCode())) {
            if (caseRepository.existsByCodeAndIdNot(request.getCode(), cases.getId())) {
                throwSystemError("code", CaseErrorCode.CASE_CODE_EXISTED.getMessage());
            }
            cases.setCode(request.getCode());
        }
    }

    private void updateCaseStatus(CaseUpdateRequest request, Cases cases) throws ApiException {
        if (request.getStatusId() != null) {
            CaseStatus caseStatus = getCaseStatusById(request.getStatusId());
            cases.setCase_status(caseStatus);
        }
    }

    private void updateCaseType(CaseUpdateRequest request, Cases cases) {
        if (request.getType() != null) {
            cases.setCaseType(request.getType());
        }
    }

    private void updateCaseDescription(CaseUpdateRequest request, Cases cases) {
        if (request.getDescription() != null) {
            cases.setDescription(request.getDescription());
        }
    }

    @Override
    public HashMap<String, String> updateCase(Long id, CaseUpdateRequest request)
            throws ApiException, ValidationException {
        Cases cases = getCaseById(id);
        Accounts accounts = SecurityUtils.getUser();

        validateAccountPermissionCase(accounts, cases);

        //        updateCaseName(request, cases);
        if (!request.getName().equals(cases.getName())) {
            cases.setName(request.getName());
        }
        updateCaseCode(request, cases);
        updateCaseStatus(request, cases);
        updateCaseType(request, cases);
        updateCaseDescription(request, cases);
        cases.setActualTime(request.getActualTime().atStartOfDay());
        caseRepository.save(cases);

        historyService.SaveHistory(
                accounts,
                ActionContent.UPDATE_CASE,
                ObjectTableType.CASE,
                cases.getId(),
                cases.getName(),
                IconType.CASE.name(),
                cases.getId());

        return new HashMap<>();
    }

    private boolean hasPermissionCreateCase(
            Accounts accounts, CaseCreateRequest caseCreateRequest, Departments departments) throws ApiException {
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())) {
            if (departments.getCode().equals(Department.PB_LANH_DAO.name())
                    || departments.getCode().equals(Department.PB_KY_THUAT.name())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
            }
            return true;
        }
        if (accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name())) {
            return accounts.getDepartments().getId().equals(caseCreateRequest.getDepartmentId());
        }
        if (accounts.getRoles().getCode().equals(Role.PHO_PHONG.name())
                || accounts.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name())) {
            return accounts.getDepartments().getId().equals(caseCreateRequest.getDepartmentId())
                    && accounts.getIsCreateCase();
        }
        return false;
    }

    private Documents getDocumentById(Long id) throws ApiException {
        return documentRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Override
    public HashMap<String, String> updateInvestigator(Long id, CaseCitizenUpdateRequest request) throws ApiException {
        Accounts accounts = SecurityUtils.getUser();
        Cases cases = getCaseById(id);
        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))) {

            if (accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name())
                    && !cases.getDepartments()
                            .getId()
                            .equals(accounts.getDepartments().getId())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
            }

            if (accounts.getRoles().getCode().equals(Role.PHO_PHONG.name())
                    || accounts.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name())) {
                if (!cases.getDepartments()
                        .getId()
                        .equals(accounts.getDepartments().getId())) {
                    throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
                }
                if (!Boolean.TRUE.equals(accounts.getIsCreateCase())) {
                    throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
                }
                Optional<AccountCase> accountCases =
                        accountCaseRepository.getAccountAccessTrue(accounts.getId(), cases.getId());
                if (accountCases.isEmpty()) {
                    throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
                }
            }
        }

        List<Long> isCheckedTrue = request.getListCitizens().stream()
                .filter(CaseCitizenRequest::getIsChecked)
                .map(CaseCitizenRequest::getId)
                .toList();

        if (!Collections.isEmpty(isCheckedTrue)) {
            List<Citizens> citizens = citizenRepository.findAllById(isCheckedTrue);
            if (citizens.size() != isCheckedTrue.size()) {
                throw new ApiException(CaseErrorCode.CASE_CITIZEN_NOT_FOUND_IN_LIST);
            }

            List<CasePerson> casePersons = citizens.stream()
                    .map(citizen -> CasePerson.builder()
                            .type(CasePersonType.INVESTIGATOR.name())
                            .cases(cases)
                            .citizens(citizen)
                            .build())
                    .collect(Collectors.toList());

            List<CasePerson> casePeople = casePersonRepository.findExistingCasePersons(
                    List.of(CasePersonType.INVESTIGATOR.name()), id, isCheckedTrue);
            if (!Collections.isEmpty(casePeople)) {
                casePersons.removeIf(
                        newCasePerson -> casePeople.stream().anyMatch(existingCasePerson -> existingCasePerson
                                .getCitizens()
                                .getId()
                                .equals(newCasePerson.getCitizens().getId())));
            }
            casePersonRepository.saveAll(casePersons);
        }

        List<Long> isCheckedFalse = request.getListCitizens().stream()
                .filter(caseCitizenRequest -> !caseCitizenRequest.getIsChecked())
                .map(CaseCitizenRequest::getId)
                .toList();
        if (!Collections.isEmpty(isCheckedFalse)) {
            casePersonRepository.deleteCitizenInCase(id, isCheckedFalse, List.of(CasePersonType.INVESTIGATOR.name()));
        }

        historyService.SaveHistory(
                accounts,
                ActionContent.UPDATE_INVESTIGATOR_IN_CASE,
                ObjectTableType.CASE,
                cases.getId(),
                cases.getName(),
                IconType.CASE.name(),
                cases.getId());

        return new HashMap<>();
    }

    @Override
    public HashMap<String, String> updateProsecutorList(
            CasePosition type, Long id, List<CaseAccountUpdateRequest> casePersons) throws ApiException {
        Cases cases = getCaseById(id);
        Accounts accounts = SecurityUtils.getUser();

        boolean existsPermission = false;
        if (accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name())) {
            if (!accounts.getDepartments().getId().equals(cases.getDepartments().getId())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
            }
            existsPermission = true;
        }
        boolean exists = false;
        if (accounts.getRoles().getCode().equals(Role.PHO_PHONG.name())
                || accounts.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name())) {
            if (!accounts.getDepartments().getId().equals(cases.getDepartments().getId())
                    || Boolean.TRUE.equals(!accounts.getIsCreateCase())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
            }
            exists = accountCaseRepository.existsByHasAccessAndIsCreateCaseForUser(accounts.getId(), id);
            if (!exists) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
            }
        }
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())
                || exists
                || existsPermission) {
            // Sử dụng map để lấy id từ mỗi phần tử
            List<Long> listIdsCreate = casePersons.stream()
                    .filter(CaseAccountUpdateRequest::getIsChecked)
                    .map(CaseAccountUpdateRequest::getId)
                    .distinct()
                    .collect(Collectors.toList());

            // Lấy ra account trong hệ thống
            List<Accounts> listAccountEntity = accountRepository.findByIdsAndIddepart(
                    listIdsCreate, cases.getDepartments().getId());
            if (listIdsCreate.size() > listAccountEntity.size()) {
                throw new ApiException(CaseErrorCode.ACCOUNT_NOT_FOUND_IN_LIST);
            }
            // Tạo mới accountCase
            List<AccountCase> accountCaseList = new ArrayList<>();
            listAccountEntity.forEach(f -> {
                AccountCase accountCase = AccountCase.builder()
                        .hasAccess(true)
                        .isProsecutor(type.equals(CasePosition.PROSECUTOR))
                        .isInCharge(type.equals(CasePosition.INCHARGE))
                        .accountRole(f.getRoles().getCode())
                        .accounts(f)
                        .cases(cases)
                        .hasPermissionDownload(false)
                        .build();
                accountCaseList.add(accountCase);
            });

            // Kiểm tra AC/C đã tồn tại
            List<AccountCase> listACEntity =
                    accountCaseRepository.findAccountCasesByAccountIdsAndCaseId(listIdsCreate, id);
            if (!CollectionUtils.isEmpty(listACEntity)) {
                Map<Long, AccountCase> mapListAccNew = accountCaseList.stream()
                        .collect(Collectors.toMap(f -> f.getAccounts().getId(), f -> f));
                listACEntity.forEach(f -> {
                    if (mapListAccNew.containsKey(f.getAccounts().getId())) {
                        AccountCase accountCase =
                                mapListAccNew.get(f.getAccounts().getId());
                        accountCaseList.remove(accountCase);
                    }

                    if (type.equals(CasePosition.PROSECUTOR)) {
                        f.setHasAccess(true);
                        f.setIsProsecutor(true);
                    }
                    if (type.equals(CasePosition.INCHARGE)) {
                        f.setHasAccess(true);
                        f.setIsInCharge(true);
                    }
                });
            }
            accountCaseRepository.saveAll(listACEntity);
            accountCaseRepository.saveAll(accountCaseList);

            // Lọc những đối tượng có isCheck là false
            List<Long> listIdsRemove = casePersons.stream()
                    .filter(request -> !request.getIsChecked())
                    .map(CaseAccountUpdateRequest::getId)
                    .distinct()
                    .toList();
            List<AccountCase> listAccountRemoveEntity =
                    accountCaseRepository.findAccountCasesByAccountIdsAndCaseId(listIdsRemove, cases.getId());
            listAccountRemoveEntity.forEach(f -> {
                if (type.equals(CasePosition.PROSECUTOR) && Boolean.FALSE.equals(f.getIsInCharge())
                        || type.equals(CasePosition.INCHARGE) && Boolean.FALSE.equals(f.getIsProsecutor())) {
                    f.setHasAccess(false);
                    f.setIsProsecutor(false);
                    f.setIsInCharge(false);
                    f.setHasPermissionDownload(false);
                } else if (type.equals(CasePosition.PROSECUTOR) && Boolean.TRUE.equals(f.getIsInCharge())) {
                    f.setHasAccess(true);
                    f.setIsProsecutor(false);
                } else if (type.equals(CasePosition.INCHARGE) && Boolean.TRUE.equals(f.getIsProsecutor())) {
                    f.setHasAccess(true);
                    f.setIsInCharge(false);
                }

                f.setAccountRole(f.getAccounts().getRoles().getCode());
            });
            accountCaseRepository.saveAll(listAccountRemoveEntity);
        }

        historyService.SaveHistory(
                accounts,
                ActionContent.UPDATE_PROSECUTOR_INCHARGE_IN_CASE,
                ObjectTableType.CASE,
                cases.getId(),
                cases.getName(),
                IconType.CASE.name(),
                cases.getId());

        return new HashMap<>();
    }

    @Override
    public HashMap<String, String> updateSuspectAndDefendant(Long id, CaseCitizenUpdateRequest request)
            throws ApiException {
        Accounts accounts = SecurityUtils.getUser();
        Cases cases = getCaseById(id);
        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))) {

            if (accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name())
                    && !cases.getDepartments()
                            .getId()
                            .equals(accounts.getDepartments().getId())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
            }

            if (accounts.getRoles().getCode().equals(Role.PHO_PHONG.name())
                    || accounts.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name())) {
                if (!cases.getDepartments()
                        .getId()
                        .equals(accounts.getDepartments().getId())) {
                    throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
                }
                if (!Boolean.TRUE.equals(accounts.getIsCreateCase())) {
                    throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
                }
                Optional<AccountCase> accountCases =
                        accountCaseRepository.getAccountAccessTrue(accounts.getId(), cases.getId());
                if (accountCases.isEmpty()) {
                    throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
                }
            }
        }

        List<Long> isCheckedTrue = request.getListCitizens().stream()
                .filter(CaseCitizenRequest::getIsChecked)
                .map(CaseCitizenRequest::getId)
                .distinct()
                .toList();

        if (!Collections.isEmpty(isCheckedTrue)) {
            List<Citizens> citizens = citizenRepository.findAllById(isCheckedTrue);
            if (citizens.size() != isCheckedTrue.size()) {
                throw new ApiException(CaseErrorCode.CASE_CITIZEN_NOT_FOUND_IN_LIST);
            }

            List<CasePerson> casePersons = citizens.stream()
                    .map(citizen -> CasePerson.builder()
                            .type(CasePersonType.SUSPECT.name())
                            .cases(cases)
                            .citizens(citizen)
                            .build())
                    .collect(Collectors.toList());

            List<CasePerson> casePeople = casePersonRepository.findExistingCasePersons(
                    List.of(CasePersonType.SUSPECT.name(), CasePersonType.DEFENDANT.name()), id, isCheckedTrue);
            if (!Collections.isEmpty(casePeople)) {
                casePersons.removeIf(
                        newCasePerson -> casePeople.stream().anyMatch(existingCasePerson -> existingCasePerson
                                .getCitizens()
                                .getId()
                                .equals(newCasePerson.getCitizens().getId())));
            }
            casePersonRepository.saveAll(casePersons);
        }

        List<Long> isCheckedFalse = request.getListCitizens().stream()
                .filter(caseCitizenRequest -> !caseCitizenRequest.getIsChecked())
                .map(CaseCitizenRequest::getId)
                .distinct()
                .toList();
        if (!Collections.isEmpty(isCheckedFalse)) {
            casePersonRepository.deleteCitizenInCase(
                    id, isCheckedFalse, List.of(CasePersonType.SUSPECT.name(), CasePersonType.DEFENDANT.name()));
        }

        historyService.SaveHistory(
                accounts,
                ActionContent.UPDATE_SUSPECT_DEFENDANT_LIST,
                ObjectTableType.CASE,
                cases.getId(),
                cases.getName(),
                IconType.CASE.name(),
                cases.getId());

        return new HashMap<>();
    }

    @Override
    public HashMap<String, String> updateTypeCasePerson(Long id, CaseCitizenUpdateRequest request) throws ApiException {
        Cases cases = getCaseById(id);
        Accounts loginAccount = SecurityUtils.getUser();
        checkRoleEditPersons(loginAccount, cases);

        List<Long> suspectIds = request.getListCitizens().stream()
                .filter(f -> f.getType().equals(CasePersonType.SUSPECT))
                .map(CaseCitizenRequest::getId)
                .distinct()
                .toList();

        List<Long> defendantIds = request.getListCitizens().stream()
                .filter(f -> f.getType().equals(CasePersonType.DEFENDANT))
                .map(CaseCitizenRequest::getId)
                .distinct()
                .toList();

        List<CasePerson> personSave = new ArrayList<>();

        if (!Collections.isEmpty(suspectIds)) {
            List<CasePerson> persons = casePersonRepository.getSuspectAndDefendant(
                    cases.getId(), suspectIds, CasePersonType.DEFENDANT.name());
            persons.forEach(person -> person.setType(CasePersonType.SUSPECT.name()));
            personSave.addAll(persons);
        }

        if (!Collections.isEmpty(defendantIds)) {
            List<CasePerson> persons = casePersonRepository.getSuspectAndDefendant(
                    cases.getId(), defendantIds, CasePersonType.SUSPECT.name());
            persons.forEach(person -> person.setType(CasePersonType.DEFENDANT.name()));
            personSave.addAll(persons);
        }

        if (!Collections.isEmpty(personSave)) {
            casePersonRepository.saveAll(personSave);
        }

        historyService.SaveHistory(
                loginAccount,
                ActionContent.UPDATE_SUSPECT_DEFENDANT_TYPE,
                ObjectTableType.CASE,
                cases.getId(),
                cases.getName(),
                IconType.CASE.name(),
                cases.getId());
        return new HashMap<>();
    }

    private void checkRoleEditPersons(Accounts accounts, Cases cases) throws ApiException {
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())) return;
        if (!accounts.getDepartments().getId().equals(cases.getDepartments().getId())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
        }
        if (accounts.getRoles().getCode().equals(Role.PHO_PHONG.name())
                || accounts.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name())) {
            Optional<AccountCase> accountCases =
                    accountCaseRepository.getAccountAccessTrue(accounts.getId(), cases.getId());
            if (Boolean.TRUE.equals(!accounts.getIsCreateCase()) || accountCases.isEmpty()) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE_NOT_CREATE);
            }
        }
    }

    private boolean hasPermissionGetAllMindmapTemplate(Accounts accounts, Cases cases) {
        if (accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.getRoles().getCode().equals(Role.VIEN_PHO.name())) {
            return true;
        }
        if (accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name())) {
            return accounts.getDepartments()
                    .getId()
                    .equals(cases.getDepartments().getId());
        } else {
            return accounts.getDepartments()
                            .getId()
                            .equals(cases.getDepartments().getId())
                    && caseRepository.checkCaseAccess(accounts.getId(), cases.getId());
        }
    }

    @Override
    public CaseMindmapTemplateResponse<MindmapTemplateResponse> getAllMindMapTemplates(
            Long caseId, Integer page, Integer pageSize, String textSearch) throws ApiException {
        Accounts accounts = SecurityUtils.getUser();
        Cases cases = getCaseById(caseId);
        if (!hasPermissionGetAllMindmapTemplate(accounts, cases)) {
            throw new ApiException(CaseErrorCode.CASE_NOT_ACCESS);
        }
        if (page < 1 || pageSize < 1) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }
        Pageable pageable =
                PageRequest.of(page - 1, pageSize, Sort.by("updatedAt").descending());
        Page<MindmapTemplate> mindMapTemplateList = mindmapTemplateRepository.getListMindmapTemplate(
                cases.getDepartments().getId(), textSearch, pageable);
        Page<MindmapTemplateResponse> mindMapTemplateResponsesList =
                mindMapTemplateList.map(mindmap -> MindmapTemplateResponse.builder()
                        .id(mindmap.getId())
                        .name(mindmap.getName())
                        .url(mindmap.getUrl())
                        .build());
        //        historyService.SaveHistory(accounts, ActionContent.GET_ALL_MIND_MAP_TEMPLATES,ObjectTableType.CASE,
        // cases.getId(), cases.getName(),IconType.CASE.name(),cases.getId());
        return new CaseMindmapTemplateResponse<>(
                cases.getDepartments().getName(),
                mindMapTemplateResponsesList.getContent(),
                (int) mindMapTemplateResponsesList.getTotalElements(),
                mindMapTemplateResponsesList.getSize(),
                mindMapTemplateResponsesList.getNumber(),
                mindMapTemplateResponsesList.getTotalPages());
    }

    @Override
    public ResultList<AccountDownloadResponse> getListAccountNoPermissionDownload(Long id, String textSearch)
            throws ApiException {
        Accounts accounts = SecurityUtils.getUser();
        Cases cases = getCaseById(id);
        if (textSearch == null || textSearch.isBlank()) {
            textSearch = null;
        }
        List<AccountDownloadResponse> listAccount = caseRepository.findAccountsNoDownload(id, textSearch);
        List<AccountDownloadResponse> listAccRes = new ArrayList<>();
        if (!listAccount.isEmpty()) {
            listAccRes = listAccount.stream()
                    .filter(f -> f.getRoleName().equals(Role.PHO_PHONG.getName())
                            || f.getRoleName().equals(Role.KIEM_SAT_VIEN.getName()))
                    .collect(Collectors.toList());
        }

        //        historyService.SaveHistory(
        //                accounts,
        //                ActionContent.GET_ACCOUNT_DONT_HAVE_PERMISSION_DOWNLOAD_IN_CASE,
        //                ObjectTableType.CASE,
        //                cases.getId(),
        //                cases.getName(),
        //                IconType.CASE.name(),
        //                cases.getId()
        //        );

        return new ResultList<>(listAccRes);
    }

    @Override
    public HashMap<String, String> removePermissionDownloadCase(Long caseId, Long accountId) throws ApiException {
        Accounts accountLogin = SecurityUtils.getUser();
        Cases cases = getCaseById(caseId);
        getCaseById(caseId);
        Optional<Accounts> accounts = accountRepository.findById(accountId);
        if (accounts.isEmpty()) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }
        AccountCase accountCase = accountCaseRepository
                .findByCases_IdAndAccounts_Id(caseId, accountId)
                .orElseThrow(() -> new ApiException(AccountCaseErrorCode.ACCOUNT_CASE_NOT_FOUND));
        if (accounts.get().getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                || accounts.get().getRoles().getCode().equals(Role.VIEN_PHO.name())
                || accounts.get().getRoles().getCode().equals(Role.TRUONG_PHONG.name())) {
            String msg = MessageFormat.format(
                    CaseErrorCode.NOT_REMOVE_PERMISSION_DOWNLOAD_CASE.getMessage(),
                    String.join(", ", accounts.get().getRoles().getName()));
            throw new ApiException(CaseErrorCode.NOT_REMOVE_PERMISSION_DOWNLOAD_CASE, msg);
        }
        accountCase.setHasPermissionDownload(false);
        accountCaseRepository.save(accountCase);

        historyService.SaveHistory(
                accountLogin,
                ActionContent.REMOVE_DOWNLOAD_PERMISSION_IN_CASE,
                ObjectTableType.CASE,
                cases.getId(),
                cases.getName(),
                IconType.CASE.name(),
                cases.getId());

        return new HashMap<>();
    }

    @Override
    public ResultList<AccountDownloadCaseResponse> grantPermissionDownloadCase(Long caseId, Set<Long> accountIds)
            throws ApiException {
        Accounts accountLogin = SecurityUtils.getUser();
        Cases cases = getCaseById(caseId);
        getCaseById(caseId);
        if (accountIds.isEmpty()) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }

        List<Accounts> accountList = accountRepository.findAllById(accountIds);
        List<AccountDownloadCaseResponse> listResponse = accountList.stream()
                .map(acc -> {
                    Optional<AccountCase> accountCaseOptional =
                            accountCaseRepository.findByCases_IdAndAccounts_Id(caseId, acc.getId());
                    if (accountCaseOptional.isEmpty()) {
                        return AccountDownloadCaseResponse.builder()
                                .id(acc.getId())
                                .username(acc.getUsername())
                                .fullName(acc.getProfile().getFullName())
                                .hasPermission(false)
                                .reason(ResponseMessage.ACCOUNT_NOT_JOIN_IN_CASE)
                                .build();
                    }

                    boolean isLeader = acc.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                            || acc.getRoles().getCode().equals(Role.VIEN_PHO.name())
                            || acc.getRoles().getCode().equals(Role.TRUONG_PHONG.name());
                    if (isLeader) {
                        String reason = ResponseMessage.YOU_NOT_ACTION_WITH_LEADER + " "
                                + acc.getRoles().getName();
                        return AccountDownloadCaseResponse.builder()
                                .id(acc.getId())
                                .username(acc.getUsername())
                                .fullName(acc.getProfile().getFullName())
                                .hasPermission(false)
                                .reason(reason)
                                .build();
                    }

                    if (accountCaseOptional.get().getHasAccess().equals(false)) {
                        accountCaseOptional.get().setHasPermissionDownload(false);
                        accountCaseRepository.save(accountCaseOptional.get());
                        return AccountDownloadCaseResponse.builder()
                                .id(acc.getId())
                                .username(acc.getUsername())
                                .fullName(acc.getProfile().getFullName())
                                .hasPermission(false)
                                .reason(ResponseMessage.ACCOUNT_IS_OUT_CASE)
                                .build();
                    }

                    accountCaseOptional.get().setHasPermissionDownload(true);
                    accountCaseRepository.save(accountCaseOptional.get());
                    return AccountDownloadCaseResponse.builder()
                            .id(acc.getId())
                            .username(acc.getUsername())
                            .fullName(acc.getProfile().getFullName())
                            .hasPermission(true)
                            .reason(ResponseMessage.GRANT_PERMISSION_DOWNLOAD_SUCCESS)
                            .build();
                })
                .toList();

        historyService.SaveHistory(
                accountLogin,
                ActionContent.GRANT_DOWNLOAD_PERMISSION_TO_ACCOUNT_IN_CASE,
                ObjectTableType.CASE,
                cases.getId(),
                cases.getName(),
                IconType.CASE.name(),
                cases.getId());

        return new ResultList<>(listResponse);
    }

    @Override
    public ResultList<AccountDownloadResponse> getListAccountHasPermissionDownload(Long caseId) throws ApiException {
        Accounts accountLogin = SecurityUtils.getUser();
        Cases cases = getCaseById(caseId);
        getCaseById(caseId);

        var entities = caseRepository.findAccountsHasPermissionDownload(caseId);
        List<AccountDownloadResponse> res = new ArrayList<>();
        if (!entities.isEmpty()) {
            entities.forEach(entity -> {
                if (entity.getRoleName().equals(Role.PHO_PHONG.getName())
                        || entity.getRoleName().equals(Role.KIEM_SAT_VIEN.getName())) {
                    res.add(entity);
                }
            });
        }
        return new ResultList<>(res);
    }
}
