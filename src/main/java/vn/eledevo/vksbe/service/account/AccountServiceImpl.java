package vn.eledevo.vksbe.service.account;

import static vn.eledevo.vksbe.constant.FileConst.AVATAR_ALLOWED_EXTENSIONS;
import static vn.eledevo.vksbe.constant.RoleCodes.*;
import static vn.eledevo.vksbe.utils.FileUtils.*;
import static vn.eledevo.vksbe.utils.SecurityUtils.getUserName;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import vn.eledevo.vksbe.constant.*;
import vn.eledevo.vksbe.constant.ErrorCodes.*;
import vn.eledevo.vksbe.dto.model.account.AccountDetailResponse;
import vn.eledevo.vksbe.dto.model.account.AccountQueryToFilter;
import vn.eledevo.vksbe.dto.model.account.UserInfo;
import vn.eledevo.vksbe.dto.request.AccountActive;
import vn.eledevo.vksbe.dto.request.AccountRequest;
import vn.eledevo.vksbe.dto.request.PinChangeRequest;
import vn.eledevo.vksbe.dto.request.account.*;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.ResultList;
import vn.eledevo.vksbe.dto.response.ResultUrl;
import vn.eledevo.vksbe.dto.response.account.*;
import vn.eledevo.vksbe.dto.response.computer.ComputerResponse;
import vn.eledevo.vksbe.dto.response.computer.ConnectComputerResponse;
import vn.eledevo.vksbe.dto.response.usb.UsbConnectedResponse;
import vn.eledevo.vksbe.entity.*;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.repository.*;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.FileUtils;
import vn.eledevo.vksbe.utils.SecurityUtils;
import vn.eledevo.vksbe.utils.minio.MinioProperties;
import vn.eledevo.vksbe.utils.minio.MinioService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountServiceImpl implements AccountService {
    AccountRepository accountRepository;
    TokenRepository tokenRepository;
    PasswordEncoder passwordEncoder;
    ComputerRepository computerRepository;
    UsbRepository usbRepository;
    RoleRepository roleRepository;
    DepartmentRepository departmentRepository;
    OrganizationRepository organizationRepository;
    ProfileRepository profileRepository;
    MinioService minioService;
    MinioProperties minioProperties;
    HistoryService historyService;
    AccountCaseRepository accountCaseRepository;

    @Value("${app.host}")
    @NonFinal
    private String appHost;

    private Accounts validAccount(Long id) throws ApiException {
        return accountRepository.findById(id).orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
    }

    /**
     * Đặt lại mật khẩu cho tài khoản được chỉ định.
     * <p>
     * Chức năng:
     * - Đặt mật khẩu mới giống với tên đăng nhập (username)
     * - Xóa mã PIN
     * - Đặt lại các điều kiện đăng nhập (isConditionLogin1 và isConditionLogin2) về false
     * - Cập nhật thời gian và người thực hiện thay đổi
     * - Xóa tất cả token liên quan đến tài khoản
     *
     * @param id Id của tài khoản cần đặt lại mật khẩu
     * @return AccountResponse chứa thông tin tài khoản sau khi cập nhật
     * @throws ApiException nếu có lỗi xảy ra trong quá trình xử lý
     */
    @Override
    @Transactional
    public HashMap<String, String> resetPassword(Long id) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        Accounts accounts = validAccount(id);
        accounts.setPassword(passwordEncoder.encode(accounts.getUsername()));
        accounts.setPin(null);
        accounts.setIsConditionLogin1(false);
        accounts.setIsConditionLogin2(false);
        accounts.setStatus(String.valueOf(Status.INACTIVE));
        accountRepository.save(accounts);
        tokenRepository.deleteByAccounts_Id(id);
        historyService.SaveHistory(
                loginAccount,
                ActionContent.RESET_PASSWORD,
                ObjectTableType.ACCOUNT,
                accounts.getId(),
                accounts.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return new HashMap<>();
    }

    private Boolean isBoss(Accounts accSecurity) {
        return switch (accSecurity.getRoles().getCode()) {
            case IT_ADMIN, VIEN_TRUONG, VIEN_PHO -> true;
            default -> false;
        };
    }

    @Override
    @Transactional
    public ResponseFilter<AccountResponseByFilter> getListAccountByFilter(
            AccountRequest accountRequest, Integer currentPage, Integer limit) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        if ((loginAccount.getRoles().getCode().equals(Role.TRUONG_PHONG.name())
                        || loginAccount.getRoles().getCode().equals(Role.PHO_PHONG.name()))
                && !loginAccount.getDepartments().getId().equals(accountRequest.getDepartmentId())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }

        Page<AccountResponseByFilter> filters = getListAccount(loginAccount, accountRequest, currentPage, limit);

        return new ResponseFilter<>(
                filters.getContent(),
                (int) filters.getTotalElements(),
                filters.getSize(),
                filters.getNumber(),
                filters.getTotalPages());
    }

    private Page<AccountResponseByFilter> getListAccount(
            Accounts loginAccount, AccountRequest accountRequest, Integer currentPage, Integer limit) {
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (limit < 1) {
            limit = 10;
        }
        Pageable pageable =
                PageRequest.of(currentPage - 1, limit, Sort.by("updatedAt").descending());
        if (accountRequest.getFromDate() == null) {
            accountRequest.setFromDate(LocalDate.of(1900, 1, 1));
        }
        if (accountRequest.getToDate() == null) {
            accountRequest.setToDate(LocalDate.now());
        }
        if (accountRequest.getFromDate().isAfter(accountRequest.getToDate())) {
            return Page.empty(pageable);
        }
        if (Boolean.FALSE.equals(isBoss(loginAccount))) {
            accountRequest.setDepartmentId(loginAccount.getDepartments().getId());
        }
        Page<AccountQueryToFilter> page =
                accountRepository.getAccountList(accountRequest, isBoss(loginAccount), pageable);
        return page.map(account -> {
            checkRoleToShowButton(account, loginAccount);
            return AccountResponseByFilter.builder()
                    .id(account.getId())
                    .username(account.getUsername())
                    .fullName(account.getFullName())
                    .roleName(account.getRoleName())
                    .isCreateCase(account.getIsCreateCase())
                    .departmentName(account.getDepartmentName())
                    .organizationName(account.getOrganizationName())
                    .status(account.getStatus())
                    .createdAt(account.getCreatedAt().toLocalDate())
                    .updatedAt(account.getUpdatedAt().toLocalDate())
                    .isShowUnlockButton(account.getIsShowUnlockButton())
                    .isEnabledUnlockButton(account.getIsEnabledUnlockButton())
                    .isShowLockButton(account.getIsShowLockButton())
                    .isEnabledLockButton(account.getIsEnabledLockButton())
                    .isShowPermissionCreateCaseButton(account.getIsShowPermissionCreateCaseButton())
                    .isEnabledPermissionCreateCaseButton(account.getIsEnabledPermissionCreateCaseButton())
                    .isShowRemovePermissionCreateCaseButton(account.getIsShowRemovePermissionCreateCaseButton())
                    .isEnabledRemovePermissionCreateCaseButton(account.getIsEnabledRemovePermissionCreateCaseButton())
                    .build();
        });
    }

    private void checkRoleToShowButton(AccountQueryToFilter account, Accounts accSecurity) {
        Role viewedAccRole = Role.valueOf(account.getRoleCode());
        Role loginAccRole = Role.valueOf(accSecurity.getRoles().getCode());

        if (account.getStatus().equals(Status.ACTIVE.name())
                && priorityRoles(loginAccRole) > priorityRoles(viewedAccRole)) {
            account.setIsShowLockButton(true);
            account.setIsEnabledLockButton(true);
        }

        // Tài khoản ở trạng thái không hoạt khộng
        boolean accountNotActive = account.getStatus().equals(Status.INACTIVE.name())
                || account.getStatus().equals(Status.INITIAL.name());

        if (Boolean.TRUE.equals(accountNotActive
                && priorityRoles(loginAccRole) > priorityRoles(viewedAccRole)
                && account.getIsConnectComputer()
                && account.getIsConnectUsb())) {
            account.setIsShowUnlockButton(true);
            account.setIsEnabledUnlockButton(true);
        }

        boolean isSameLoginRole = loginAccRole.equals(Role.TRUONG_PHONG) || loginAccRole.equals(Role.PHO_PHONG);

        if (isSameLoginRole
                && accSecurity.getDepartments().getId().equals(account.getDepartmentId())
                && account.getStatus().equals(Status.ACTIVE.name())
                && priorityRoles(loginAccRole) > priorityRoles(viewedAccRole)) {
            account.setIsShowLockButton(true);
            account.setIsEnabledLockButton(true);
        }
        if (Boolean.TRUE.equals(isSameLoginRole
                && accountNotActive
                && priorityRoles(loginAccRole) > priorityRoles(viewedAccRole)
                && account.getIsConnectComputer()
                && account.getIsConnectUsb())) {
            account.setIsShowUnlockButton(true);
            account.setIsEnabledUnlockButton(true);
        }

        // Tài khoản không đủ điều kiện (Chưa được liên kết máy tính hoặc usb)
        boolean accountInValid = Boolean.FALSE.equals(account.getIsConnectComputer() && account.getIsConnectUsb());

        if (accountInValid
                && priorityRoles(loginAccRole) > priorityRoles(viewedAccRole)
                && account.getStatus().equals(Status.ACTIVE.name())) {
            account.setIsShowLockButton(true);
        }
        if (accountInValid && priorityRoles(loginAccRole) > priorityRoles(viewedAccRole) && accountNotActive) {
            account.setIsShowUnlockButton(true);
        }

        boolean isRoleCreateCase = loginAccRole.equals(Role.VIEN_TRUONG)
                || loginAccRole.equals(Role.VIEN_PHO)
                || loginAccRole.equals(Role.TRUONG_PHONG);

        if (Boolean.TRUE.equals(isRoleCreateCase && account.getIsCreateCase())
                && (account.getRoleCode().equals(Role.PHO_PHONG.name())
                        || account.getRoleCode().equals(Role.KIEM_SAT_VIEN.name()))) {
            account.setIsEnabledRemovePermissionCreateCaseButton(true);
            account.setIsShowRemovePermissionCreateCaseButton(true);
        }

        if (Boolean.TRUE.equals(isRoleCreateCase && !account.getIsCreateCase())
                && (account.getRoleCode().equals(Role.PHO_PHONG.name())
                        || account.getRoleCode().equals(Role.KIEM_SAT_VIEN.name()))) {
            account.setIsEnabledPermissionCreateCaseButton(true);
            account.setIsShowPermissionCreateCaseButton(true);
        }
    }

    @Override
    public ResultList<ComputerResponse> getComputersByIdAccount(Long accountId) throws ApiException {
        if (!accountRepository.existsById(accountId)) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        List<Computers> res = computerRepository.findByAccounts_Id(accountId);
        List<ComputerResponse> list = res.stream()
                .map(computers -> ComputerResponse.builder()
                        .id(computers.getId())
                        .name(computers.getName())
                        .code(computers.getCode())
                        .type(computers.getType())
                        .build())
                .toList();
        return new ResultList<>(list);
    }

    @Override
    @Transactional
    public HashMap<String, String> inactivateAccount(Long idAccount) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        String userName = SecurityUtils.getUserName();
        // account đang đăng nhập
        Optional<AccountActive> accountLogin = accountRepository.findByUsernameActive(userName);

        // check account đang đăng nhập có tồn tại không
        if (accountLogin.isEmpty()) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }

        Accounts lockAccount = accountRepository
                .findById(idAccount)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));

        // check id request có trùng với người khóa
        if (lockAccount.getId().equals(accountLogin.get().getId())) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }

        // Role của người đăng nhập
        String roleCode = accountLogin.get().getRoleCode();
        // Role của người cần khóa
        String lockAccountRole = lockAccount.getRoles().getCode();
        boolean sameDepartment = accountLogin
                .get()
                .getDepartmentId()
                .equals(lockAccount.getDepartments().getId());

        switch (roleCode) {
            case VIEN_TRUONG, VIEN_PHO, IT_ADMIN -> handleLeader(Role.valueOf(roleCode), Role.valueOf(lockAccountRole));
            case TRUONG_PHONG, PHO_PHONG -> handleMember(
                    Role.valueOf(roleCode), Role.valueOf(lockAccountRole), sameDepartment);
            default -> throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        lockAccount.setStatus(Status.INACTIVE.name());
        accountRepository.save(lockAccount);
        tokenRepository.deleteByAccounts_Id(idAccount);
        historyService.SaveHistory(
                loginAccount,
                ActionContent.LOCK_ACCOUNT,
                ObjectTableType.ACCOUNT,
                lockAccount.getId(),
                lockAccount.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return new HashMap<>();
    }

    private Void handleLeader(Role roleLogin, Role lockAccountRole) throws ApiException {
        if (priorityRoles(roleLogin) <= priorityRoles(lockAccountRole)) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }
        return null;
    }

    private void handleMember(Role roleLogin, Role lockAccountRole, boolean sameDepartment) throws ApiException {
        if ((priorityRoles(roleLogin) > priorityRoles(lockAccountRole)) && sameDepartment) {
            return;
        }
        throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
    }

    @Override
    @Transactional
    public HashMap<String, String> removeConnectComputer(Long accountId, Long computerId) throws ApiException {
        Accounts loginAcc = SecurityUtils.getUser();
        Accounts accounts = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        Computers computers = computerRepository
                .findById(computerId)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        if (computers.getAccounts().getRoles().getCode().equals(Role.IT_ADMIN.name())) {
            throw new ApiException(ComputerErrorCode.NOT_REMOVE_ROLE_IT_ADMIN);
        }
        if (!computers.getAccounts().getId().equals(accountId)) {
            throw new ApiException(ComputerErrorCode.PC_NOT_LINKED_TO_ACCOUNT);
        }
        computers.setAccounts(null);
        computers.setStatus(Status.DISCONNECTED.name());
        computerRepository.save(computers);
        int soThietBiKetNoi = accounts.getComputers().size();
        if (Objects.equals(soThietBiKetNoi, 1)) {
            accounts.setIsConnectComputer(false);
            accountRepository.save(accounts);
        }
        // gỡ usb token
        Optional<Usbs> usb = usbRepository.findByAccounts_Id(accountId);
        if (usb.isPresent()) {
            removeUSB(accountId, usb.get().getId());
        }
        historyService.SaveHistory(
                loginAcc,
                ActionContent.REMOVE_ACCOUNT_WITH_COMPUTER,
                ObjectTableType.ACCOUNT,
                accounts.getId(),
                accounts.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return new HashMap<>();
    }

    @Override
    public ResultList<UsbConnectedResponse> getUsbInfo(Long id) throws ApiException {
        validAccount(id);
        return new ResultList<>(usbRepository.findUsbConnectedByAccountId(id));
    }

    @Override
    @Transactional
    public ResultList<ConnectComputerResponse> connectComputers(Long id, Set<Long> computerIds) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        Accounts accounts = validAccount(id);
        List<ConnectComputerResponse> computerResponses = new ArrayList<>();
        if (!CollectionUtils.isEmpty(computerIds)) {
            List<Computers> computers = computerRepository.findByIdIn(computerIds);

            List<Long> currentLinkedComputerIds = computerRepository.findComputerIdsByAccountId(id);

            List<Computers> connectedComputers = new ArrayList<>();
            Map<Long, Computers> computersMap = computers.stream().collect(Collectors.toMap(Computers::getId, c -> c));
            if (CollectionUtils.isEmpty(computers)) {
                throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
            }
            if (accounts.getRoles().getCode().equals(Role.IT_ADMIN.name())) {
                throw new ApiException(ComputerErrorCode.NOT_REMOVE_ROLE_IT_ADMIN);
            }
            for (Long computerId : computerIds) {
                if (computersMap.get(computerId) != null) {
                    String nameUpdate = SecurityUtils.getUserName();
                    var computer = computersMap.get(computerId);
                    if (computer.getStatus().equals(Status.CONNECTED.name())
                            && currentLinkedComputerIds.contains(computerId)) {
                        computerResponses.add(ConnectComputerResponse.builder()
                                .id(computer.getId())
                                .name(computer.getName())
                                .computerCode(computer.getCode())
                                .isConnected(false)
                                .reason(ResponseMessage.COMPUTER_CONNECTED_WITH_THIS_ACCOUNT)
                                .build());
                    } else if (computer.getStatus().equals(Status.CONNECTED.name())
                            && !currentLinkedComputerIds.contains(computerId)) {
                        computerResponses.add(ConnectComputerResponse.builder()
                                .id(computer.getId())
                                .name(computer.getName())
                                .computerCode(computer.getCode())
                                .isConnected(false)
                                .reason(ResponseMessage.COMPUTER_CONNECTED_WITH_ANOTHER_ACCOUNT)
                                .build());
                    } else {
                        computerResponses.add(ConnectComputerResponse.builder()
                                .id(computer.getId())
                                .name(computer.getName())
                                .computerCode(computer.getCode())
                                .isConnected(true)
                                .reason(ResponseMessage.COMPUTER_CONNECTED_SUCCESS)
                                .build());
                        computer.setAccounts(accounts);
                        computer.setStatus(Status.CONNECTED.name());
                        computer.setUpdatedBy(nameUpdate);
                        connectedComputers.add(computer);
                        accounts.setIsConnectComputer(true);
                        accountRepository.save(accounts);
                        Optional<Usbs> usb = usbRepository.findByAccounts_Id(id);
                        if (usb.isPresent()) {
                            removeUSB(id, usb.get().getId());
                        }
                    }
                } else {
                    computerResponses.add(ConnectComputerResponse.builder()
                            .id(computerId)
                            .name(null)
                            .computerCode(null)
                            .isConnected(false)
                            .reason(ResponseMessage.COMPUTER_NOT_FOUND_SYSTEM)
                            .build());
                }
            }
            if (!connectedComputers.isEmpty()) {
                computerRepository.saveAll(connectedComputers);
            }
        }
        historyService.SaveHistory(
                loginAccount,
                ActionContent.CONNECT_ACCOUNT_WITH_COMPUTER,
                ObjectTableType.ACCOUNT,
                accounts.getId(),
                accounts.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);

        return new ResultList<>(computerResponses);
    }

    @Override
    @Transactional
    public HashMap<String, String> removeConnectUSB(Long accountID, Long usbID) throws ApiException {
        removeUSB(accountID, usbID);
        return new HashMap<>();
    }

    @Override
    public ActivedAccountResponse activeAccount(Long id) throws ApiException {
        Accounts activeAcc = validAccount(id);
        Accounts loginAcc = validAccount(SecurityUtils.getUserId());

        Role loginAccRole = Role.valueOf(loginAcc.getRoles().getCode());
        Role activedAccRole = Role.valueOf(activeAcc.getRoles().getCode());
        boolean isSameDepartment = loginAcc.getDepartments()
                .getId()
                .equals(activeAcc.getDepartments().getId());
        if (priorityRoles(loginAccRole) <= priorityRoles(activedAccRole)) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }
        if (activedAccRole.equals(Role.VIEN_TRUONG) || activedAccRole.equals(Role.TRUONG_PHONG)) {
            Optional<AccountSwapResponse> accountsLeader = accountRepository.getOldLeader(
                    activedAccRole.name(), activeAcc.getDepartments().getCode());
            if (accountsLeader.isPresent()
                    && !activeAcc.getId().equals(accountsLeader.get().getId())) {
                AccountErrorCode.ACCOUNT_LIST_EXIT.setResult(accountsLeader);
                throw new ApiException(AccountErrorCode.ACCOUNT_LIST_EXIT);
            }
        }
        if (loginAccRole.equals(Role.TRUONG_PHONG) && !isSameDepartment
                || loginAccRole.equals(Role.PHO_PHONG) && !isSameDepartment) {
            throw new ApiException(AccountErrorCode.DEPARTMENT_CONFLICT);
        }
        if (Boolean.FALSE.equals(activeAcc.getIsConnectComputer())) {
            throw new ApiException(AccountErrorCode.ACCOUNT_NOT_LINKED_TO_COMPUTER);
        }

        if (Boolean.FALSE.equals(activeAcc.getIsConnectUsb())) {
            throw new ApiException(AccountErrorCode.ACCOUNT_NOT_LINKED_TO_USB);
        }
        activeAcc.setStatus(Status.ACTIVE.name());
        accountRepository.save(activeAcc);
        historyService.SaveHistory(
                loginAcc,
                ActionContent.ACTIVE_ACCOUNT,
                ObjectTableType.ACCOUNT,
                activeAcc.getId(),
                activeAcc.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return new ActivedAccountResponse();
    }

    @Override
    public AccountSwapResponse swapStatus(Long newAccountId, Long oldAccountId) throws ApiException {
        return swap(newAccountId, oldAccountId);
    }

    private AccountSwapResponse swap(Long newAccountId, Long oldAccountId) throws ApiException {
        Accounts loginAcc = SecurityUtils.getUser();
        Accounts existingAccount = accountRepository
                .findById(newAccountId)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        if (!existingAccount.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !existingAccount.getRoles().getCode().equals(Role.TRUONG_PHONG.name())) {
            throw new ApiException(RoleErrorCode.CURRENT_ROLE_NOT_CHANGEABLE);
        }

        Long departmentId = existingAccount.getDepartments().getId();
        String roleCode = existingAccount.getRoles().getCode();
        Optional<Accounts> accountLeadOptional =
                accountRepository.findByDepartment(departmentId, roleCode, Status.ACTIVE.name());
        if (accountLeadOptional.isEmpty()) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        Accounts accountLead = accountLeadOptional.get();
        if (!accountLead.getId().equals(oldAccountId)) {
            AccountSwapResponse accountLeadResponse = AccountSwapResponse.builder()
                    .id(accountLead.getId())
                    .username(accountLead.getUsername())
                    .fullName(accountLead.getProfile().getFullName())
                    .build();
            AccountErrorCode.ACCOUNT_LIST_EXIT.setResult(Optional.of(accountLeadResponse));
            throw new ApiException(AccountErrorCode.ACCOUNT_LIST_EXIT);
        }

        accountLead.setStatus(Status.INACTIVE.name());
        existingAccount.setStatus(Status.ACTIVE.name());
        accountRepository.save(existingAccount);
        accountRepository.save(accountLead);
        historyService.SaveHistory(
                loginAcc,
                ActionContent.SWAP_ACCOUNT,
                ObjectTableType.ACCOUNT,
                existingAccount.getId(),
                existingAccount.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return AccountSwapResponse.builder().build();
    }

    @Override
    @Transactional
    public AccountResponse createAccountInfo(AccountCreateRequest request) throws ValidationException, ApiException {
        Accounts loginAcc = SecurityUtils.getUser();

        Roles newAccountRole = roleRepository
                .findById(request.getRoleId())
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        Departments newAccountDepartment = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));

        validateRoleAndDepartment(newAccountRole, newAccountDepartment);

        Map<String, String> errors = validateAccountCreateRequest(request);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Profiles profile = createProfile(request);
        Accounts account = createAccount(request, profile);
        profile.setAccounts(account);
        profileRepository.save(profile);
        Accounts savedAccount = accountRepository.save(account);

        historyService.SaveHistory(
                loginAcc,
                ActionContent.CREATE_ACCOUNT,
                ObjectTableType.ACCOUNT,
                savedAccount.getId(),
                savedAccount.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return AccountResponse.builder().id(savedAccount.getId()).build();
    }

    private void validateRoleAndDepartment(Roles newAccountRole, Departments newAccountDepartment) throws ApiException {
        Accounts curLoginAcc = SecurityUtils.getUser();

        if (!curLoginAcc.getRoles().getCode().equals(Role.IT_ADMIN.name())
                || newAccountRole.getCode().equals(Role.IT_ADMIN.name())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }
        if (newAccountDepartment.getCode().equals(Department.PB_KY_THUAT.name())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }
        if (Boolean.FALSE.equals(isAllowedToCreateAccount(newAccountRole, newAccountDepartment))) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }
    }

    @Override
    public ResultUrl uploadAvatar(MultipartFile file) throws Exception {
        validateAvatarFile(file);
        return new ResultUrl(minioService.uploadFile(file));
    }

    @Override
    @Transactional
    public AccountSwapResponse updateAccountInfo(Long updatedAccId, AccountUpdateRequest req) throws Exception {
        Accounts accountLogin = SecurityUtils.getUser();
        Accounts accountUpdate = validAccount(updatedAccId);

        Roles requestRole = roleRepository
                .findById(req.getRoleId())
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        Departments requestDepartment = departmentRepository
                .findById(req.getDepartmentId())
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));

        if (requestRole.getCode().equals(Role.IT_ADMIN.name())
                || requestDepartment.getCode().equals(Department.PB_KY_THUAT.name())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }

        int priorityRoleUpdate =
                priorityRoles(Role.valueOf(accountUpdate.getRoles().getCode()));
        int priorityRoleLogin =
                priorityRoles(Role.valueOf(accountLogin.getRoles().getCode()));
        if (priorityRoleLogin <= priorityRoleUpdate) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }

        if (Boolean.FALSE.equals(isAllowedToCreateAccount(requestRole, requestDepartment))) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }

        if ((!requestRole.getCode().equals(Role.VIEN_TRUONG.name())
                        && !requestRole.getCode().equals(Role.TRUONG_PHONG.name()))
                || !accountUpdate.getStatus().equals(Status.ACTIVE.name())) {
            accountToUpdate(req, updatedAccId, requestRole);
            //            changePermissionDownLoadCase(req, accountUpdate);
            historyService.SaveHistory(
                    accountLogin,
                    ActionContent.UPDATE_ACCOUNT,
                    ObjectTableType.ACCOUNT,
                    accountUpdate.getId(),
                    accountUpdate.getProfile().getFullName(),
                    IconType.ACCOUNT.name(),
                    null);
            return AccountSwapResponse.builder().build();
        }

        AccountSwapResponse oldPositionAccInfo = accountRepository.getOldPositionAccInfo(req.getDepartmentId());
        if (Objects.equals(oldPositionAccInfo, null)) {
            accountToUpdate(req, updatedAccId, requestRole);
            //            changePermissionDownLoadCase(req, accountUpdate);
            historyService.SaveHistory(
                    accountLogin,
                    ActionContent.UPDATE_ACCOUNT,
                    ObjectTableType.ACCOUNT,
                    accountUpdate.getId(),
                    accountUpdate.getProfile().getFullName(),
                    IconType.ACCOUNT.name(),
                    null);
            return AccountSwapResponse.builder().build();
        }

        if ((req.getSwappedAccId() == 0 && updatedAccId.equals(oldPositionAccInfo.getId()))
                || (req.getSwappedAccId() != 0
                        && updatedAccId.equals(oldPositionAccInfo.getId())
                        && oldPositionAccInfo.getId().equals(req.getSwappedAccId()))) {
            accountToUpdate(req, updatedAccId, requestRole);
        }

        if (req.getSwappedAccId() != 0
                && !oldPositionAccInfo.getId().equals(updatedAccId)
                && oldPositionAccInfo.getId().equals(req.getSwappedAccId())) {
            Accounts accountLead =
                    accountRepository.findById(req.getSwappedAccId()).orElseThrow();
            accountLead.setStatus(Status.INACTIVE.name());
            accountRepository.save(accountLead);

            Accounts account = accountToUpdate(req, updatedAccId, requestRole);
            account.setStatus(Status.ACTIVE.name());
            accountRepository.save(account);
        }

        if (!oldPositionAccInfo.getId().equals(req.getSwappedAccId())
                && !updatedAccId.equals(oldPositionAccInfo.getId())) {
            AccountErrorCode.ACCOUNT_LIST_EXIT.setResult(Optional.of(oldPositionAccInfo));
            throw new ApiException(AccountErrorCode.ACCOUNT_LIST_EXIT);
        }
        //        changePermissionDownLoadCase(req, accountUpdate);
        historyService.SaveHistory(
                accountLogin,
                ActionContent.UPDATE_ACCOUNT,
                ObjectTableType.ACCOUNT,
                accountUpdate.getId(),
                accountUpdate.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return AccountSwapResponse.builder().build();
    }

    //    private void changePermissionDownLoadCase(AccountUpdateRequest req, Accounts accountUpdate) throws
    // ApiException {
    //        if (!req.getDepartmentId().equals(accountUpdate.getDepartments().getId())) {
    //            accountCaseRepository.updateHasPermissionDownloadFalseByAccountIdAndDepartmentId(
    //                    accountUpdate.getId(), accountUpdate.getDepartments().getId());
    //        }
    //
    //        Roles requestRole = roleRepository
    //                .findById(req.getRoleId())
    //                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
    //        boolean isLeader = accountUpdate.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
    //                || accountUpdate.getRoles().getCode().equals(Role.VIEN_PHO.name())
    //                || accountUpdate.getRoles().getCode().equals(Role.TRUONG_PHONG.name());
    //        boolean isNotLeader = requestRole.getCode().equals(Role.PHO_PHONG.name())
    //                || requestRole.getCode().equals(Role.KIEM_SAT_VIEN.name());
    //        //Nếu chỉnh sửa từ role VT, VP, TP xuống role PP, KSV
    //        if(isLeader && isNotLeader){
    //            accountCaseRepository.updateHasPermissionDownloadFalseByAccountId(accountUpdate.getId());
    //        }
    //        //Nếu chỉnh sửa từ role PP, KSV lên role VT, VP, TP
    //        if(!isLeader && !isNotLeader){
    //            accountCaseRepository.updateHasPermissionDownloadTrueByAccountId(accountUpdate.getId());
    //        }
    //    }

    @Override
    public UserInfo userInfo() throws ApiException {
        Long userId = SecurityUtils.getUserId();
        Optional<UserInfo> userDetail = accountRepository.findAccountProfileById(userId);
        if (userDetail.isEmpty()) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        return userDetail.get();
    }

    @Override
    @Transactional
    public AccountResponse updateAvatarUserInfo(Long id, AvatarRequest request) throws Exception {
        Accounts accountLogin = SecurityUtils.getUser();
        Map<String, String> error = new HashMap<>();
        validateAvatar(request.getAvatar(), error);
        if (!error.isEmpty()) {
            throw new ApiException(SystemErrorCode.VALIDATE_FORM);
        }
        Accounts account =
                accountRepository.findById(id).orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        if (account.getProfile().getAvatar() != null
                && !account.getProfile().getAvatar().isEmpty()) {
            minioService.deleteFile(account.getProfile().getAvatar());
        }
        account.getProfile().setAvatar(request.getAvatar());
        account.getProfile().setUriName(FileUtils.getUriName(request.getAvatar()));
        accountRepository.save(account);
        historyService.SaveHistory(
                accountLogin,
                ActionContent.UPDATE_AVATAR_ACCOUNT,
                ObjectTableType.ACCOUNT,
                account.getId(),
                account.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return AccountResponse.builder().build();
    }

    @Override
    public HashMap<String, String> changePinUserLogin(PinChangeRequest pinRequest)
            throws ApiException, ValidationException {
        Map<String, String> errors = new HashMap<>();
        String userName = getUserName();
        Accounts accountRequest = accountRepository
                .findAccountInSystem(userName)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        if (!accountRequest.getStatus().equals(Status.ACTIVE.name())) {
            throw new ApiException(AccountErrorCode.ACCOUNT_INACTIVE);
        }
        if (Boolean.FALSE.equals(accountRequest.getIsConditionLogin2())) {
            throw new ApiException(AccountErrorCode.CHANGE_PIN_LOGIN);
        }
        if (!passwordEncoder.matches(pinRequest.getOldPinCode(), accountRequest.getPin())) {
            errors.put("oldPinCode", ResponseMessage.OLD_PIN_INCORRECT);
            throw new ValidationException(errors);
        }
        if (pinRequest.getOldPinCode().equals(pinRequest.getNewPinCode())) {
            errors.put("newPinCode", ResponseMessage.NEW_PIN_SAME_AS_OLD);
            throw new ValidationException(errors);
        }
        if (!pinRequest.getNewPinCode().equals(pinRequest.getConfirmPinCode())) {
            errors.put("newPinCode", ResponseMessage.CONFIRM_PIN_MISMATCH);
            throw new ValidationException(errors);
        }
        String hashPin = passwordEncoder.encode(pinRequest.getConfirmPinCode());
        accountRequest.setPin(hashPin);
        accountRepository.save(accountRequest);
        HashMap<String, String> result = new HashMap<>();
        result.put("pin", hashPin);

        historyService.SaveHistory(
                accountRequest,
                ActionContent.UPDATE_PIN_ACCOUNT,
                ObjectTableType.ACCOUNT,
                accountRequest.getId(),
                accountRequest.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return result;
    }

    private void removeUSB(Long accountID, Long usbID) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();

        Accounts account = accountRepository
                .findById(accountID)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));

        if (Role.valueOf(account.getRoles().getCode()).equals(Role.IT_ADMIN)) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }

        if (!Objects.equals(account.getUsb().getId(), usbID)) {
            throw new ApiException(AccountErrorCode.ACCOUNT_NOT_LINKED_TO_USB);
        }

        Optional<Usbs> usbToken = usbRepository.findById(usbID);
        if (usbToken.isEmpty()) {
            throw new ApiException(AccountErrorCode.ACCOUNT_NOT_LINKED_TO_USB);
        }
        usbToken.get().setAccounts(null);
        usbToken.get().setStatus(Status.DISCONNECTED.name());
        usbToken.get().setKeyUsb(null);
        usbRepository.save(usbToken.get());
        account.setIsConnectUsb(false);
        account.setIsConditionLogin2(false);
        account.setStatus(Status.INACTIVE.name());
        accountRepository.save(account);
        tokenRepository.deleteByAccounts_Id(accountID);

        historyService.SaveHistory(
                loginAccount,
                ActionContent.REMOVE_USB,
                ObjectTableType.ACCOUNT,
                account.getId(),
                account.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
    }

    private void validateRoleForViewButton(
            AccountDetailResponse detailResponse, Accounts accSecurity, Accounts account) {
        Role loginAcc = Role.valueOf(accSecurity.getRoles().getCode());
        Role detailedAcc = Role.valueOf(account.getRoles().getCode());
        boolean isLoginAdminAndDetailedNotAdmin = loginAcc.equals(Role.IT_ADMIN) && !detailedAcc.equals(Role.IT_ADMIN);

        if (isLoginAdminAndDetailedNotAdmin) {
            detailResponse.setIsEnabledEditButton(true);
            detailResponse.setIsShowEditButton(true);
            detailResponse.setIsEnabledResetPasswordButton(true);
            detailResponse.setIsShowResetPasswordButton(true);
        }

        if (isLoginAdminAndDetailedNotAdmin || priorityRoles(loginAcc) > priorityRoles(detailedAcc)) {
            detailResponse.setIsEnabledLockButton(true);
        }
        if (account.getStatus().equals(Status.ACTIVE.name()) && priorityRoles(loginAcc) > priorityRoles(detailedAcc)) {
            detailResponse.setIsShowLockButton(true);
        }
        if ((isLoginAdminAndDetailedNotAdmin || priorityRoles(loginAcc) > priorityRoles(detailedAcc))
                && account.getIsConnectUsb().equals(Boolean.TRUE)
                && account.getIsConnectComputer().equals(Boolean.TRUE)) {
            detailResponse.setIsEnabledActivateButton(true);
        }
        if (!account.getStatus().equals(Status.ACTIVE.name()) && priorityRoles(loginAcc) > priorityRoles(detailedAcc)) {
            detailResponse.setIsShowActivateButton(true);
        }
        if ((loginAcc.equals(Role.TRUONG_PHONG) || loginAcc.equals(Role.PHO_PHONG))
                && priorityRoles(loginAcc) > priorityRoles(detailedAcc)
                && accSecurity
                        .getDepartments()
                        .getId()
                        .equals(account.getDepartments().getId())
                && account.getStatus().equals(Status.ACTIVE.name())) {
            detailResponse.setIsShowLockButton(true);
            detailResponse.setIsEnabledLockButton(true);
        }

        boolean isRoleCreateCase = loginAcc.equals(Role.VIEN_TRUONG)
                || loginAcc.equals(Role.VIEN_PHO)
                || loginAcc.equals(Role.TRUONG_PHONG);

        if (Boolean.TRUE.equals(isRoleCreateCase && account.getIsCreateCase())
                && (account.getRoles().getCode().equals(Role.PHO_PHONG.name())
                        || account.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name()))) {
            detailResponse.setIsEnabledRemovePermissionCreateCaseButton(true);
            detailResponse.setIsShowRemovePermissionCreateCaseButton(true);
        }

        if (Boolean.TRUE.equals(isRoleCreateCase && !account.getIsCreateCase())
                && (account.getRoles().getCode().equals(Role.PHO_PHONG.name())
                        || account.getRoles().getCode().equals(Role.KIEM_SAT_VIEN.name()))) {
            detailResponse.setIsEnabledPermissionCreateCaseButton(true);
            detailResponse.setIsShowPermissionCreateCaseButton(true);
        }
    }

    private Boolean isAllowedToCreateAccount(Roles newAccountRole, Departments newAccountDepartment) {
        List<String> roleLead = List.of(Role.VIEN_TRUONG.name(), Role.VIEN_PHO.name());

        if (newAccountDepartment.getCode().equals(Department.PB_LANH_DAO.name()))
            return roleLead.contains(newAccountRole.getCode());

        return !newAccountRole.getCode().equals(Role.VIEN_TRUONG.name())
                && !newAccountRole.getCode().equals(Role.VIEN_PHO.name());
    }

    private Map<String, String> validateAccountCreateRequest(AccountCreateRequest request) {
        Map<String, String> errors = new HashMap<>();

        validateUsername(request.getUsername(), errors);
        validateRole(request.getRoleId(), request.getRoleName(), errors);
        validateDepartment(request.getDepartmentId(), request.getDepartmentName(), errors);
        validateOrganization(request.getOrganizationId(), request.getOrganizationName(), errors);
        validateAvatar(request.getAvatar(), errors);

        return errors;
    }

    private void validateUsername(String username, Map<String, String> errors) {
        if (accountRepository.existsByUsername(username)) {
            errors.put("username", ResponseMessage.USERNAME_IS_EXIST);
        }
    }

    private void validateRole(Long roleId, String roleName, Map<String, String> errors) {
        Optional<Roles> rolesOptional = roleRepository.findById(roleId);

        if (rolesOptional.isEmpty()) {
            errors.put("roleId", ResponseMessage.ROLE_NOT_EXIST);
        } else {
            Roles role = rolesOptional.get();
            if (!Objects.equals(role.getName(), roleName)) {
                errors.put("roleName", ResponseMessage.OUTDATED_DATA);
            }
        }
    }

    private void validateDepartment(Long departmentId, String departmentName, Map<String, String> errors) {
        Optional<Departments> departmentsOptional = departmentRepository.findById(departmentId);

        if (departmentsOptional.isEmpty()) {
            errors.put("departmentId", ResponseMessage.DEPARTMENT_NOT_EXIST);
        } else {
            Departments department = departmentsOptional.get();
            if (!Objects.equals(department.getName(), departmentName)) {
                errors.put("departmentName", ResponseMessage.OUTDATED_DATA);
            }
        }
    }

    private void validateOrganization(Long organizationId, String organizationName, Map<String, String> errors) {
        Optional<Organizations> organizationsOptional = organizationRepository.findById(organizationId);

        if (organizationsOptional.isEmpty()) {
            errors.put("organizationId", ResponseMessage.ORGANIZATION_NOT_EXIST);
        } else {
            Organizations organization = organizationsOptional.get();
            if (!Objects.equals(organization.getName(), organizationName)) {
                errors.put("organizationName", ResponseMessage.OUTDATED_DATA);
            }
        }
    }

    private Profiles createProfile(AccountCreateRequest request) {
        return Profiles.builder()
                .fullName(request.getFullName())
                .avatar(request.getAvatar())
                .phoneNumber(request.getPhoneNumber())
                .uriName(FileUtils.getUriName(request.getAvatar()))
                .gender(request.getGender())
                .build();
    }

    private Accounts createAccount(AccountCreateRequest request, Profiles profile) {
        Roles roleRequest = roleRepository.findById(request.getRoleId()).orElseThrow();
        boolean isCreateCase = roleRequest.getCode().equals(Role.VIEN_TRUONG.name())
                || roleRequest.getCode().equals(Role.VIEN_PHO.name())
                || roleRequest.getCode().equals(Role.TRUONG_PHONG.name());
        return Accounts.builder()
                .username(request.getUsername())
                .roles(roleRequest)
                .departments(
                        departmentRepository.findById(request.getDepartmentId()).orElseThrow())
                .password(passwordEncoder.encode(request.getUsername()))
                .isConditionLogin1(false)
                .isConditionLogin2(false)
                .isConnectComputer(false)
                .isConnectUsb(false)
                .status(Status.INITIAL.name())
                .profile(profile)
                .isCreateCase(isCreateCase)
                .build();
    }

    public void validateAvatar(String avatarUrl, Map<String, String> errors) {
        if (StringUtils.isBlank(avatarUrl)) {
            return;
        }
        String keyError = "avatar";

        try {
            URI uri = new URI(avatarUrl);

            String scheme = uri.getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                errors.put(keyError, ResponseMessage.AVATAR_URL_INVALID);
                return;
            }

            String host = uri.getHost();
            if (host == null || !appHost.contains(host)) {
                errors.put(keyError, ResponseMessage.AVATAR_URL_INVALID);
                return;
            }

            String path = uri.getPath();
            if (path == null || !path.contains("/" + minioProperties.getBucketName() + "/")) {
                errors.put(keyError, ResponseMessage.AVATAR_URL_INVALID);
                return;
            }

            if (!isPathAllowedExtension(path, AVATAR_ALLOWED_EXTENSIONS)) {
                errors.put(keyError, ResponseMessage.AVATAR_URL_INVALID);
            }

        } catch (URISyntaxException e) {
            errors.put("avatar", ResponseMessage.AVATAR_URL_INVALID);
        }
    }

    private Accounts accountToUpdate(AccountUpdateRequest req, Long updatedAccId, Roles updateAccRole)
            throws Exception {
        Profiles profile = profileRepository.findByAccounts_Id(updatedAccId);
        Accounts account = accountRepository.findById(updatedAccId).orElseThrow();

        if (!Department.valueOf(account.getDepartments().getCode()).equals(Department.PB_LANH_DAO)
                && !account.getDepartments().getId().equals(req.getDepartmentId())) {
            accountCaseRepository.updateAccountCaseByAccountId(
                    updatedAccId, account.getDepartments().getId());
        }
        if (Department.valueOf(account.getDepartments().getCode()).equals(Department.PB_LANH_DAO)
                && !account.getDepartments().getId().equals(req.getDepartmentId())) {
            accountCaseRepository.updateAccountCaseByAccountIdAndDepartmentId(updatedAccId, req.getDepartmentId());
        }
        if (account.getDepartments().getId().equals(req.getDepartmentId())
                && (req.getRoleName().equals(Role.PHO_PHONG.getName())
                        || req.getRoleName().equals(Role.KIEM_SAT_VIEN.getName()))) {
            if (!req.getRoleName().equals(account.getRoles().getName())) {
                accountCaseRepository.updateHasPermissionDownloadFalseByAccountId(updatedAccId, req.getDepartmentId());
            }
        }
        profile.setFullName(req.getFullName());
        profile.setPhoneNumber(req.getPhoneNumber());
        profile.setGender(req.getGender());
        if (profile.getAvatar() != null
                && !profile.getAvatar().isEmpty()
                && !req.getAvatar().equals(profile.getAvatar())) {
            minioService.deleteFile(profile.getAvatar());
        }
        // update anh
        profile.setAvatar(req.getAvatar());
        profile.setUriName(FileUtils.getUriName(req.getAvatar()));
        Profiles profileSave = profileRepository.save(profile);
        if (!account.getRoles().getCode().equals(updateAccRole.getCode())
                || !account.getDepartments().getId().equals(req.getDepartmentId())) {
            boolean isCreateCase = updateAccRole.getCode().equals(Role.VIEN_TRUONG.name())
                    || updateAccRole.getCode().equals(Role.VIEN_PHO.name())
                    || updateAccRole.getCode().equals(Role.TRUONG_PHONG.name());
            account.setIsCreateCase(isCreateCase);
        }
        account.setProfile(profileSave);
        account.setRoles(updateAccRole);
        account.setDepartments(
                departmentRepository.findById(req.getDepartmentId()).orElseThrow());
        if (account.getRoles().getCode().equals(updateAccRole.getCode())) {
            tokenRepository.deleteByAccounts_Id(updatedAccId);
        }
        return accountRepository.save(account);
    }

    private void validateAvatarFile(MultipartFile file) throws ValidationException {
        Map<String, String> errors = new HashMap<>();
        if (file == null || file.isEmpty()) {
            return;
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedExtension(fileExtension, FileConst.AVATAR_ALLOWED_EXTENSIONS)) {
            String msg = MessageFormat.format(
                    ResponseMessage.AVATAR_INVALID_FORMAT, String.join(", ", FileConst.AVATAR_ALLOWED_EXTENSIONS));
            errors.put("avatar", msg);
            throw new ValidationException(errors);
        }

        if (file.getSize() > FileConst.MAX_AVATAR_SIZE * FileConst.BYTES_IN_MB) {
            String msg = MessageFormat.format(ResponseMessage.AVATAR_SIZE_EXCEEDS_LIMIT, FileConst.MAX_AVATAR_SIZE);
            errors.put("avatar", msg);
            throw new ValidationException(errors);
        }
    }

    private int priorityRoles(Role role) {
        return switch (role) {
            case IT_ADMIN -> 11;
            case VIEN_TRUONG -> 10;
            case VIEN_PHO -> 9;
            case TRUONG_PHONG -> 8;
            case PHO_PHONG -> 7;
            default -> 0;
        };
    }

    @Override
    public HashMap<String, String> grantPermissionToCreateCase(Long id, GrantedPermissionRequest req)
            throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        Accounts viewedAccount = validAccount(id);

        Role viewedAccRole = Role.valueOf(viewedAccount.getRoles().getCode());
        Role loginAccRole = Role.valueOf(loginAccount.getRoles().getCode());

        if (canGrantPermission(loginAccRole, viewedAccRole, loginAccount, viewedAccount)) {
            viewedAccount.setIsCreateCase(req.getIsCreateCase());
            accountRepository.save(viewedAccount);
        } else {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }
        if (Boolean.FALSE.equals(req.getIsCreateCase())) {
            historyService.SaveHistory(
                    loginAccount,
                    ActionContent.REMOVE_PERMISSION_CREATE_A_CASE,
                    ObjectTableType.ACCOUNT,
                    viewedAccount.getId(),
                    viewedAccount.getProfile().getFullName(),
                    IconType.ACCOUNT.name(),
                    null);
        } else {
            historyService.SaveHistory(
                    loginAccount,
                    ActionContent.AUTHORIZATION_TO_CREATE_A_CASE,
                    ObjectTableType.ACCOUNT,
                    viewedAccount.getId(),
                    viewedAccount.getProfile().getFullName(),
                    IconType.ACCOUNT.name(),
                    null);
        }
        return new HashMap<>();
    }

    private boolean canGrantPermission(
            Role loginAccRole, Role viewedAccRole, Accounts loginAccount, Accounts viewedAccount) {
        if (loginAccRole.equals(Role.VIEN_TRUONG) || loginAccRole.equals(Role.VIEN_PHO)) {
            return true;
        }
        return loginAccRole.equals(Role.TRUONG_PHONG)
                && loginAccount
                        .getDepartments()
                        .getId()
                        .equals(viewedAccount.getDepartments().getId())
                && priorityRoles(loginAccRole) > priorityRoles(viewedAccRole);
    }
}
