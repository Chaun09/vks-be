package vn.eledevo.vksbe.service.case_flow;

import static vn.eledevo.vksbe.constant.ActionContent.*;
import static vn.eledevo.vksbe.constant.FileConst.AVATAR_ALLOWED_EXTENSIONS;
import static vn.eledevo.vksbe.utils.FileUtils.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import vn.eledevo.vksbe.constant.*;
import vn.eledevo.vksbe.constant.ErrorCodes.CaseErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.CaseFlowErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.MindmapTemplateErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.constant.IconType;
import vn.eledevo.vksbe.constant.ObjectTableType;
import vn.eledevo.vksbe.constant.ResponseMessage;
import vn.eledevo.vksbe.constant.Role;
import vn.eledevo.vksbe.dto.request.case_flow.CaseFlowCreateRequest;
import vn.eledevo.vksbe.dto.request.case_flow.CaseFlowUpdateRequest;
import vn.eledevo.vksbe.dto.response.ResultUrl;
import vn.eledevo.vksbe.dto.response.case_flow.CaseFlowResponse;
import vn.eledevo.vksbe.entity.*;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.repository.*;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.service.histories.HistoryServiceImpl;
import vn.eledevo.vksbe.utils.FileUtils;
import vn.eledevo.vksbe.utils.SecurityUtils;
import vn.eledevo.vksbe.utils.minio.MinioProperties;
import vn.eledevo.vksbe.utils.minio.MinioService;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CaseFlowServiceImpl implements CaseFlowService {
    DepartmentRepository departmentRepository;
    CaseFlowRepository caseFlowRepository;
    CaseRepository caseRepository;
    MindmapTemplateRepository mindmapTemplateRepository;
    AccountCaseRepository accountCaseRepository;
    MinioProperties minioProperties;
    MinioService minioService;
    AccountRepository accountRepository;
    HistoryServiceImpl historyServiceImpl;
    HistoryService historyService;

    @Value("${app.host}")
    @NonFinal
    private String appHost;

    private void checkUser(Accounts loginAccount, Cases cases) throws ApiException {
        if (!loginAccount.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !loginAccount.getRoles().getCode().equals(Role.VIEN_PHO.name())) {
            if (!loginAccount
                    .getDepartments()
                    .getId()
                    .equals(cases.getDepartments().getId())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
            }
            if (!loginAccount.getRoles().getCode().equals(Role.TRUONG_PHONG.name())) {
                Optional<AccountCase> accountCase = accountCaseRepository.findFirstAccountCaseByAccounts_IdAndCases_Id(
                        loginAccount.getId(), cases.getId());
                if ((accountCase.isEmpty()) || (accountCase.get().getHasAccess().equals(false))) {
                    throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION_CASE);
                }
            }
        }
    }

    @Override
    public CaseFlowResponse getCaseFlow(Long caseId) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        Cases cases = caseRepository.findById(caseId).orElseThrow(() -> new ApiException(CaseErrorCode.CASE_NOT_FOUND));
        this.checkUser(loginAccount, cases);
        CaseFlow caseFlow = caseFlowRepository.findFirstByCases_Id(caseId).orElse(null);

        if (caseFlow == null) {
            return new CaseFlowResponse();
        }
        Optional<Accounts> accountCreate = Optional.ofNullable(accountRepository
                .findByUsername(caseFlow.getCreatedBy())
                .orElseThrow(() -> new ApiException(SystemErrorCode.NOT_FOUND_SERVER)));
        Optional<Accounts> accountUpdate = Optional.ofNullable(accountRepository
                .findByUsername(caseFlow.getUpdatedBy())
                .orElseThrow(() -> new ApiException(SystemErrorCode.NOT_FOUND_SERVER)));

        //        historyService.SaveHistory(loginAccount, ActionContent.VIEW_CASE_FLOW, ObjectTableType.CASE,
        // cases.getId(), cases.getName(), IconType.CASE.name(), cases.getId());

        return CaseFlowResponse.builder()
                .id(caseFlow.getId())
                .name(caseFlow.getName())
                .url(caseFlow.getUrl())
                .createdAt(caseFlow.getCreatedAt().toLocalDate())
                .updatedBy(accountUpdate
                        .map(accounts -> accounts.getProfile().getFullName() + " - " + caseFlow.getUpdatedBy())
                        .orElseGet(caseFlow::getUpdatedBy))
                .createdBy(accountCreate
                        .map(accounts -> accounts.getProfile().getFullName() + " - " + caseFlow.getCreatedBy())
                        .orElseGet(caseFlow::getCreatedBy))
                .build();
    }

    @Override
    public CaseFlowResponse addCaseFlow(Long caseId, CaseFlowCreateRequest caseFlowCreateRequest) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        Cases cases = caseRepository.findById(caseId).orElseThrow(() -> new ApiException(CaseErrorCode.CASE_NOT_FOUND));

        if (caseFlowRepository.findFirstByCases_Id(caseId).isPresent()) {
            throw new ApiException(CaseFlowErrorCode.CASE_FLOW_IS_PRESENTED);
        }

        // Kiểm tra quyền truy cập của người dùng
        this.checkUser(loginAccount, cases);

        CaseFlow caseFlow;
        Long templateId = caseFlowCreateRequest.getMindmapTemplateId();

        if (templateId == null || templateId == 0) {
            // Xây dựng CaseFlow khi không có Mindmap Template
            caseFlow = CaseFlow.builder()
                    .name("Sơ đồ không có tiêu đề")
                    .cases(cases)
                    .build();
        } else {
            // Tìm Mindmap Template và kiểm tra quyền
            MindmapTemplate mindmapTemplate = mindmapTemplateRepository
                    .findById(templateId)
                    .orElseThrow(() -> new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NOT_FOUND));

            if (!cases.getDepartments()
                    .getId()
                    .equals(mindmapTemplate.getDepartments().getId())) {
                throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NO_PERMISSION_TO_ACCESS);
            }

            // Xây dựng CaseFlow từ Mindmap Template
            caseFlow = CaseFlow.builder()
                    .name(mindmapTemplate.getName())
                    .dataLink(mindmapTemplate.getDataLink())
                    .dataNode(mindmapTemplate.getDataNode())
                    .url(mindmapTemplate.getUrl())
                    .uriName(mindmapTemplate.getUrl() != null ? FileUtils.getUriName(mindmapTemplate.getUrl()) : null)
                    .cases(cases)
                    .build();
        }
        // Lưu CaseFlow và tạo response
        caseFlowRepository.save(caseFlow);
        // Todo: Xử lí thêm lịch sử
        historyService.SaveHistory(
                loginAccount,
                ActionContent.ADD_CASE_FLOW,
                ObjectTableType.CASE_FLOW,
                caseFlow.getId(),
                caseFlow.getName(),
                IconType.CASE.name(),
                cases.getId());
        return new CaseFlowResponse(caseFlow.getId());
    }

    @Override
    @Transactional
    public HashMap<String, String> updateCaseFlow(Long id, Long idCaseFlow, CaseFlowUpdateRequest request)
            throws Exception {
        Accounts accounts = SecurityUtils.getUser();
        Optional<Cases> cases = caseRepository.findById(id);
        Optional<CaseFlow> caseFlow = caseFlowRepository.findById(idCaseFlow);
        if (caseFlow.isEmpty()) {
            throw new ApiException(CaseFlowErrorCode.CASE_FLOW_NOT_FOUND);
        }
        if (cases.isEmpty()) {
            throw new ApiException(CaseErrorCode.CASE_NOT_FOUND);
        }
        if (caseFlowRepository.existsCaseFlowAndCase(id, idCaseFlow).isEmpty()) {
            throw new ApiException(CaseFlowErrorCode.CASE_FLOW_NOT_MATCH_CASE);
        }

        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))) {
            if (!accounts.getDepartments()
                    .getId()
                    .equals(cases.get().getDepartments().getId())) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
            }
            if (!accounts.getRoles().getCode().equals(Role.TRUONG_PHONG.name())
                    && caseFlowRepository
                            .existsCaseFlow(
                                    idCaseFlow,
                                    accounts.getId(),
                                    accounts.getDepartments().getId())
                            .isEmpty()) {
                throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
            }
        }

        if (Objects.nonNull(request.getUrl())) {
            Map<String, String> error = new HashMap<>();
            validateUrlImg(request.getUrl(), error);
            if (!error.isEmpty()) {
                SystemErrorCode errorCode = SystemErrorCode.VALIDATE_FORM;
                errorCode.setResult(Optional.of(error));
                throw new ApiException(errorCode);
            }
            if (Objects.nonNull(caseFlow.get().getUrl())
                    && !Objects.equals(caseFlow.get().getUrl(), "")
                    && !mindmapTemplateRepository.existsByUrl(request.getUrl())
                    && !request.getUrl().equals(caseFlow.get().getUrl())) {
                minioService.deleteFile(caseFlow.get().getUrl());
            }

            caseFlow.get().setUrl(request.getUrl());
            caseFlow.get().setUriName(FileUtils.getUriName(request.getUrl()));
        }

        if (Objects.nonNull(request.getDataLink()) && !request.getDataLink().isBlank()) {
            caseFlow.get().setDataLink(request.getDataLink());
        }

        if (Objects.nonNull(request.getDataNode()) && !request.getDataNode().isBlank()) {
            caseFlow.get().setDataNode(request.getDataNode());
        }
        caseFlow.get().setName(request.getName());
        caseFlowRepository.save(caseFlow.get());
        historyService.SaveHistory(
                accounts,
                UPDATE_CASE_FLOW,
                ObjectTableType.CASE,
                id,
                cases.get().getName(),
                IconType.CASE.name(),
                id);
        return new HashMap<>();
    }

    private void validateUrlImg(String avatarUrl, Map<String, String> errors) {
        if (StringUtils.isBlank(avatarUrl)) {
            return;
        }
        String keyError = "url";

        try {
            URI uri = new URI(avatarUrl);
            String scheme = uri.getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                errors.put(keyError, ResponseMessage.MIND_MAP_IMG_URL_INVALID);
                return;
            }

            String host = uri.getHost();
            if (host == null || !appHost.contains(host)) {
                errors.put(keyError, ResponseMessage.MIND_MAP_IMG_URL_INVALID);
                return;
            }

            String path = uri.getPath();
            if (path == null || !path.contains("/" + minioProperties.getBucketName() + "/")) {
                errors.put(keyError, ResponseMessage.MIND_MAP_IMG_URL_INVALID);
                return;
            }

            if (!isPathAllowedExtension(path, AVATAR_ALLOWED_EXTENSIONS)) {
                errors.put(keyError, ResponseMessage.MIND_MAP_IMG_URL_INVALID);
            }
        } catch (URISyntaxException e) {
            errors.put("url", ResponseMessage.MIND_MAP_IMG_URL_INVALID);
        }
    }

    @Override
    public CaseFlowResponse getDetailCaseFlow(Long caseId, Long id) throws ApiException {
        Accounts loginAccount = SecurityUtils.getUser();
        Cases cases = caseRepository.findById(caseId).orElseThrow(() -> new ApiException(CaseErrorCode.CASE_NOT_FOUND));

        this.checkUser(loginAccount, cases);

        CaseFlow caseFlow = caseFlowRepository
                .findFistByCases_IdAndId(caseId, id)
                .orElseThrow(() -> new ApiException(CaseFlowErrorCode.CASE_FLOW_NOT_FOUND));

        //        historyService.SaveHistory(loginAccount, ActionContent.GET_CASE_FLOW_DETAIL,
        // ObjectTableType.CASE,cases.getId(), cases.getName(), IconType.CASE.name(), cases.getId());

        return new CaseFlowResponse(id, caseFlow.getName(), caseFlow.getDataLink(), caseFlow.getDataNode());
    }

    @Override
    public ResultUrl uploadImg(MultipartFile file, Long id) throws Exception {
        Accounts loginAccount = SecurityUtils.getUser();
        Cases cases = caseRepository.findById(id).orElseThrow(() -> new ApiException(CaseErrorCode.CASE_NOT_FOUND));
        this.checkUser(loginAccount, cases);
        validateFileImg(file);
        return new ResultUrl(minioService.uploadFile(file));
    }

    private void validateFileImg(MultipartFile file) throws ApiException {
        if (file == null || file.isEmpty()) {
            return;
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedExtension(fileExtension, FileConst.AVATAR_ALLOWED_EXTENSIONS)) {
            String msg = MessageFormat.format(
                    MindmapTemplateErrorCode.MINDMAP_IMG_INVALID_FORMAT.getMessage(),
                    String.join(", ", FileConst.AVATAR_ALLOWED_EXTENSIONS));
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_IMG_INVALID_FORMAT, msg);
        }

        if (file.getSize() > FileConst.MAX_IMG_MIND_MAP_SIZE * FileConst.BYTES_IN_MB) {
            String msg = MessageFormat.format(
                    MindmapTemplateErrorCode.MINDMAP_IMG_SIZE_EXCEEDS_LIMIT.getMessage(),
                    FileConst.MAX_IMG_MIND_MAP_SIZE);
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_IMG_SIZE_EXCEEDS_LIMIT, msg);
        }
    }
}
