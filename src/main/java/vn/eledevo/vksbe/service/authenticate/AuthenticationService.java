package vn.eledevo.vksbe.service.authenticate;

import java.io.IOException;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.config.security.JwtService;
import vn.eledevo.vksbe.constant.*;
import vn.eledevo.vksbe.constant.ErrorCodes.AccountErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.ComputerErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.UsbErrorCode;
import vn.eledevo.vksbe.dto.request.AuthenticationRequest;
import vn.eledevo.vksbe.dto.request.ChangePasswordRequest;
import vn.eledevo.vksbe.dto.request.PinRequest;
import vn.eledevo.vksbe.dto.request.TwoFactorAuthenticationRequest;
import vn.eledevo.vksbe.dto.request.account.CreateAccountTest;
import vn.eledevo.vksbe.dto.response.AuthenticationResponse;
import vn.eledevo.vksbe.dto.response.account.Token2FAResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.AuthTokens;
import vn.eledevo.vksbe.entity.Computers;
import vn.eledevo.vksbe.entity.Usbs;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.repository.*;
import vn.eledevo.vksbe.service.ChangeData;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService {
    final DepartmentRepository departmentRepository;
    final RoleRepository roleRepository;
    final AccountRepository accountRepository;
    final TokenRepository tokenRepository;
    final UsbRepository usbRepository;
    final ComputerRepository computerRepository;
    final PasswordEncoder passwordEncoder;
    final JwtService jwtService;
    final AuthenticationManager authenticationManager;
    final HistoryService historyService;

    /**
     * Đăng ký người dùng mới vào hệ thống.
     *
     * @param request Đối tượng RegisterRequest chứa thông tin đăng ký
     * @return Đối tượng AuthenticationResponse chứa token truy cập và token làm mới
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws ApiException, ValidationException {
        Map<String, String> errors = new HashMap<>();
        // Xác thực thông tin đăng nhập của người dùng
        try {
            var account = accountRepository
                    .findAccountInSystem(request.getUsername())
                    .orElseThrow(() -> {
                        errors.put("username", ResponseMessage.ACCOUNT_NOT_FOUND);
                        return new ValidationException(errors);
                    });
            // Xác thực thông tin đăng nhập của người dùng
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            if (!account.getStatus().equals(Status.ACTIVE.name())) {
                throw new ApiException(AccountErrorCode.ACCOUNT_INACTIVE);
            }
            Boolean isCheck = checkRoleItAdmin(account.getRoles().getCode());
            if (Boolean.FALSE.equals(isCheck)) {
                checkComputerForAccount(request.getCurrentDeviceId(), account.getId());
            }
            Optional<Usbs> universalSerialBus = usbRepository.findByAccounts_Id(account.getId());
            if (universalSerialBus.isEmpty()) {
                throw new ApiException(AccountErrorCode.ACCOUNT_NOT_LINKED_TO_USB);
            }
            var jwtToken = jwtService.generateToken(
                    account,
                    UUID.fromString(universalSerialBus.get().getKeyUsb()),
                    account.getRoles().getCode());
            // Hủy tất cả các token hiện có của người dùng
            revokeAllUserTokens(account);
            // Lưu token truy cập mới vào cơ sở dữ liệu
            saveUserToken(account, jwtToken, TokenType.ACCESS.toString());
            // Trả về đối tượng AuthenticationResponse chứa các token
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .departmentName(departmentRepository.getDepartmentNameById(
                            accountRepository.getDepartment(request.getUsername())))
                    .roleName(roleRepository.getRole(accountRepository.getRole(request.getUsername())))
                    .usbCode(universalSerialBus.get().getUsbCode())
                    .usbVendorCode(universalSerialBus.get().getUsbVendorCode())
                    .build();
        } catch (BadCredentialsException e) {
            errors.put("password", ResponseMessage.INVALID_ACCOUNT_OR_PASSWORD);
            throw new ValidationException(errors);
        }
    }

    /**
     * Lưu token truy cập cho người dùng vào cơ sở dữ liệu.
     *
     * @param account  Đối tượng User
     * @param jwtToken Token truy cập JWT
     */
    private void saveUserToken(Accounts account, String jwtToken, String type) {

        // Tạo đối tượng Token mới
        var token = AuthTokens.builder()
                .accounts(account)
                .token(jwtToken)
                .tokenType(type)
                .isExpiredTime(false)
                .build();

        // Lưu đối tượng Token vào cơ sở dữ liệu
        tokenRepository.save(token);
    }

    /**
     * Hủy tất cả các token hiện có của người dùng.
     *
     * @param account Đối tượng User
     */
    private void revokeAllUserTokens(Accounts account) {
        // Tìm tất cả các token hợp lệ của người dùng
        var validUserTokens = tokenRepository.findAllValidTokenByUser(account.getId());
        if (validUserTokens.isEmpty()) return;
        // Đánh dấu các token đó là hết hạn và bị hủy
        validUserTokens.forEach(token -> tokenRepository.deleteById(token.getId()));
    }

    public HashMap<String, String> createPin(PinRequest pinRequest) throws ApiException, ValidationException {
        Accounts loginAccount = SecurityUtils.getUser();
        Map<String, String> errors = new HashMap<>();
        String username = SecurityUtils.getUserName();
        Accounts account = accountRepository
                .findAccountInSystem(username)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        if (!account.getStatus().equals(Status.ACTIVE.name())) {
            throw new ApiException(AccountErrorCode.ACCOUNT_INACTIVE);
        }
        if (Boolean.TRUE.equals(account.getIsConditionLogin2())) {
            throw new ApiException(AccountErrorCode.CHANGE_FIRST_LOGIN);
        }
        if (!pinRequest.getPin().equals(pinRequest.getPin2())) {
            errors.put("pin2", ResponseMessage.PIN_CODE_MISMATCH);
            throw new ValidationException(errors);
        }
        String hashedPin = passwordEncoder.encode(pinRequest.getPin2());
        account.setPin(hashedPin);
        account.setIsConditionLogin2(Boolean.TRUE);
        accountRepository.save(account);
        HashMap<String, String> result = new HashMap<>();
        result.put("pin", hashedPin);
        historyService.SaveHistory(
                loginAccount,
                ActionContent.CREATE_PIN,
                ObjectTableType.ACCOUNT,
                loginAccount.getId(),
                loginAccount.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return result;
    }

    public HashMap<String, String> changePassword(ChangePasswordRequest request)
            throws ApiException, ValidationException {
        Accounts loginAccount = SecurityUtils.getUser();
        Map<String, String> errors = new HashMap<>();
        String userName = SecurityUtils.getUserName();
        Accounts accountRequest = accountRepository
                .findAccountInSystem(userName)
                .orElseThrow(() -> new ApiException(SystemErrorCode.INTERNAL_SERVER));
        if (!accountRequest.getStatus().equals(Status.ACTIVE.name())) {
            throw new ApiException(AccountErrorCode.ACCOUNT_INACTIVE);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), accountRequest.getPassword())) {
            errors.put("oldPassword", ResponseMessage.OLD_PASSWORD_INCORRECT);
            throw new ValidationException(errors);
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            errors.put("newPassword", ResponseMessage.NEW_PASSWORD_SAME_AS_OLD);
            throw new ValidationException(errors);
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            errors.put("confirmPassword", ResponseMessage.CONFIRM_PASSWORD_MISMATCH);
            throw new ValidationException(errors);
        }
        accountRequest.setPassword(passwordEncoder.encode(request.getConfirmPassword()));
        accountRequest.setIsConditionLogin1(Boolean.TRUE);
        accountRepository.save(accountRequest);
        historyService.SaveHistory(
                loginAccount,
                ActionContent.CHANGE_PASSWORD_FIRST,
                ObjectTableType.ACCOUNT,
                loginAccount.getId(),
                loginAccount.getProfile().getFullName(),
                IconType.ACCOUNT.name(),
                null);
        return new HashMap<>();
    }

    public AuthenticationResponse twoFactorAuthenticationRequest(TwoFactorAuthenticationRequest request)
            throws Exception {
        String employeeCode = SecurityUtils.getUserName();
        Token2FAResponse responseTokenUsb = ChangeData.decrypt(request.getTokenUsb(), Token2FAResponse.class);
        if (responseTokenUsb.getExpiredTime() > System.currentTimeMillis()) {
            throw new ApiException(SystemErrorCode.UNAUTHORIZED_SERVER);
        }
        Optional<Accounts> accounts = accountRepository.findAccountInSystem(employeeCode);
        if (accounts.isEmpty()) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        if (!accounts.get().getStatus().equals("ACTIVE")) {
            throw new ApiException(AccountErrorCode.ACCOUNT_INACTIVE);
        }
        Optional<Usbs> usbToken =
                usbRepository.usbByAccountAndConnect(accounts.get().getId());
        if (usbToken.isEmpty()) {
            throw new ApiException(UsbErrorCode.USB_NOT_BELONG_TO_ACCOUNT);
        }
        if (!request.getCurrentUsbCode().equals(responseTokenUsb.getHasString().getUsbCode())) {
            throw new ApiException(UsbErrorCode.USB_NOT_BELONG_TO_ACCOUNT);
        }
        if (!request.getCurrentUsbCode().equals(usbToken.get().getUsbCode())) {
            throw new ApiException(UsbErrorCode.USB_NOT_BELONG_TO_ACCOUNT);
        }
        if (!request.getCurrentUsbVendorCode()
                .equals(responseTokenUsb.getHasString().getUsbVendorCode())) {
            throw new ApiException(UsbErrorCode.USB_NOT_BELONG_TO_ACCOUNT);
        }
        if (!request.getCurrentUsbVendorCode().equals(usbToken.get().getUsbVendorCode())) {
            throw new ApiException(UsbErrorCode.USB_NOT_BELONG_TO_ACCOUNT);
        }
        Boolean isCheck = checkRoleItAdmin(accounts.get().getRoles().getCode());
        if (Boolean.FALSE.equals(isCheck)) {
            checkComputerForAccount(request.getCurrentDeviceId(), accounts.get().getId());
        }
        var jwtToken = jwtService.generateToken(
                accounts.get(),
                UUID.fromString(usbToken.get().getKeyUsb()),
                accounts.get().getRoles().getCode());
        var refreshToken = jwtService.generateRefreshToken(
                accounts.get(),
                UUID.fromString(usbToken.get().getKeyUsb()),
                accounts.get().getRoles().getCode());
        // Hủy tất cả các token hiện có của người dùng
        revokeAllUserTokens(accounts.get());
        // Lưu token truy cập mới vào cơ sở dữ liệu
        saveUserToken(accounts.get(), jwtToken, TokenType.ACCESS.toString());
        // Trả về đối tượng AuthenticationResponse chứa các token
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .isConditionLogin1(accounts.get().getIsConditionLogin1())
                .isConditionLogin2(accounts.get().getIsConditionLogin2())
                .build();
    }

    private Boolean checkRoleItAdmin(String role) {
        return role.equals(Role.IT_ADMIN.toString());
    }

    private void checkComputerForAccount(String deviceCode, Long accountId) throws ApiException {
        Boolean isValid = computerRepository.existsByCode(deviceCode);
        if (Boolean.FALSE.equals(isValid)) {
            throw new ApiException(ComputerErrorCode.PC_INVALID);
        }
        List<Computers> computersList = computerRepository.findByAccounts_Id(accountId);
        boolean deviceExists =
                computersList.stream().anyMatch(computer -> computer.getCode().equals(deviceCode));
        if (Boolean.FALSE.equals(deviceExists)) {
            throw new ApiException(ComputerErrorCode.PC_NOT_LINKED_TO_ACCOUNT);
        }
    }

    public CreateAccountTest createAccountTest(CreateAccountTest createAccountTest) {
        createAccountTest.setPassword(passwordEncoder.encode(createAccountTest.getPassword()));
        createAccountTest.setPinCode(passwordEncoder.encode(createAccountTest.getPinCode()));
        return createAccountTest;
    }
    /**
     * Làm mới token truy cập cho người dùng.
     *
     * @param request  Đối tượng HttpServletRequest
     * @param response Đối tượng HttpServletResponse
     * @throws IOException Ngoại lệ xảy ra khi ghi dữ liệu vào luồng đầu ra
     */
    public void refreshToken(
            jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws IOException {

        // Lấy token làm mới từ header yêu cầu
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String username;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);

        // Trích xuất email người dùng từ token làm mới
        username = jwtService.extractUsername(refreshToken);
        if (username != null) {
            var user = this.accountRepository.findByUsername(username).orElseThrow();
            // Kiểm tra tính hợp lệ của token làm mới
            if (jwtService.isTokenValid(refreshToken, user)) {
                // Nếu token làm mới hợp lệ:
                //   - Tạo token truy cập mới cho người dùng
                //   - Hủy tất cả các token hiện có của người dùng
                //   - Lưu token truy cập mới vào cơ sở dữ liệu
                //   - Trả về đối tượng AuthenticationResponse chứa các token mới
                var accessToken = jwtService.generateToken(
                        user,
                        UUID.fromString(user.getUsb().getKeyUsb()),
                        user.getRoles().getCode());
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken, TokenType.ACCESS.name());
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
