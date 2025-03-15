package vn.eledevo.vksbe.service.mindmapTemplate;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import vn.eledevo.vksbe.constant.*;
import vn.eledevo.vksbe.constant.ErrorCodes.MindmapTemplateErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.dto.request.mindmapTemplate.MindMapTemplateRequest;
import vn.eledevo.vksbe.dto.request.mindmapTemplate.MindmapTemplateUpdateRequest;
import vn.eledevo.vksbe.dto.response.MindmapTemplateResponse;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.ResultUrl;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Departments;
import vn.eledevo.vksbe.entity.MindmapTemplate;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.repository.DepartmentRepository;
import vn.eledevo.vksbe.repository.MindmapTemplateRepository;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.FileUtils;
import vn.eledevo.vksbe.utils.SecurityUtils;
import vn.eledevo.vksbe.utils.minio.MinioProperties;
import vn.eledevo.vksbe.utils.minio.MinioService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MindmapTemplateServiceImpl implements MindmapTemplateService {

    MindmapTemplateRepository mindmapTemplateRepository;
    DepartmentRepository departmentRepository;
    MinioService minioService;
    MinioProperties minioProperties;
    HistoryService historyService;

    @Value("${app.host}")
    @NonFinal
    private String appHost;

    public ResponseFilter<MindmapTemplateResponse> getListMindMapTemplate(
            Long departmentId, Integer page, Integer pageSize, String textSearch) throws ApiException {
        Accounts accounts = SecurityUtils.getUser();
        Optional<Departments> departments = departmentRepository.findById(departmentId);

        if (departments.isEmpty()) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))
                && !accounts.getDepartments().getId().equals(departmentId)) {
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NO_PERMISSION_TO_ACCESS);
        }

        if (page < 1) {
            throw new ApiException(SystemErrorCode.INVALID_PAGE_REQUEST);
        }
        if (pageSize < 1) {
            throw new ApiException(SystemErrorCode.INVALID_PAGE_REQUEST);
        }

        Pageable pageable =
                PageRequest.of(page - 1, pageSize, Sort.by("updatedAt").descending());
        Page<MindmapTemplate> mindmapTemplate =
                mindmapTemplateRepository.getListMindmapTemplate(departmentId, textSearch, pageable);
        Page<MindmapTemplateResponse> mindmapTemplateResponses =
                mindmapTemplate.map(mindmap -> MindmapTemplateResponse.builder()
                        .id(mindmap.getId())
                        .name(mindmap.getName())
                        .url(mindmap.getUrl())
                        .build());
        return new ResponseFilter<>(
                mindmapTemplateResponses.getContent(),
                (int) mindmapTemplateResponses.getTotalElements(),
                mindmapTemplateResponses.getSize(),
                mindmapTemplateResponses.getNumber(),
                mindmapTemplateResponses.getTotalPages());
    }

    @Override
    public MindmapTemplateResponse createMindMapTemplate(MindMapTemplateRequest request) throws ApiException {
        Accounts accounts = SecurityUtils.getUser();
        Optional<Departments> departments = departmentRepository.findById(request.getDepartmentId());
        if (departments.isEmpty()) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        if (!request.getDepartmentName().equals(departments.get().getName())) {
            throw new ApiException(SystemErrorCode.ORGANIZATION_STRUCTURE);
        }
        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))
                && !accounts.getDepartments().getId().equals(request.getDepartmentId())) {
            throw new ApiException(SystemErrorCode.NOT_ENOUGH_PERMISSION);
        }
        if (mindmapTemplateRepository.existsByNameAndDepartments_Id(
                request.getName(), departments.get().getId())) {
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NAME_ALREADY_EXISTS);
        }
        MindmapTemplate mindmapTemplate = MindmapTemplate.builder()
                .name(request.getName())
                .departments(departments.get())
                .build();

        mindmapTemplate = mindmapTemplateRepository.save(mindmapTemplate);

        historyService.SaveHistory(
                accounts,
                CREATE_MIND_MAP,
                ObjectTableType.MIND_MAP_TEMPLATE,
                mindmapTemplate.getId(),
                mindmapTemplate.getName(),
                IconType.MIND_MAP_TEMPLATE.name(),
                null);

        return MindmapTemplateResponse.builder()
                .id(mindmapTemplate.getId())
                .name(mindmapTemplate.getName())
                .build();
    }

    @Override
    @Transactional
    public MindmapTemplateResponse deleteMindMapTemplate(Long id) throws Exception {
        Accounts accounts = SecurityUtils.getUser();
        Optional<MindmapTemplate> mindmapTemplate = mindmapTemplateRepository.findById(id);
        if (mindmapTemplate.isEmpty()) {
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NOT_FOUND);
        }
        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))
                && !accounts.getDepartments()
                        .getId()
                        .equals(mindmapTemplate.get().getDepartments().getId())) {
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NO_PERMISSION_TO_ACCESS);
        }

        if (mindmapTemplate.get().getUrl() != null) {
            minioService.deleteFile(mindmapTemplate.get().getUrl());
        }

        String name = mindmapTemplate.get().getName();
        mindmapTemplateRepository.deleteById(id);

        historyService.SaveHistory(
                accounts,
                DELETE_MIND_MAP,
                ObjectTableType.MIND_MAP_TEMPLATE,
                id,
                name,
                IconType.MIND_MAP_TEMPLATE.name(),
                null);
        return null;
    }

    @Override
    public MindmapTemplateResponse detailMindMap(Long id) throws ApiException {
        Accounts accounts = SecurityUtils.getUser();
        Optional<MindmapTemplate> mindmapTemplate = mindmapTemplateRepository.findById(id);
        if (mindmapTemplate.isEmpty()) {
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NOT_FOUND);
        }

        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))
                && !accounts.getDepartments()
                        .getId()
                        .equals(mindmapTemplate.get().getDepartments().getId())) {
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NO_PERMISSION_TO_ACCESS);
        }

        return MindmapTemplateResponse.builder()
                .id(mindmapTemplate.get().getId())
                .name(mindmapTemplate.get().getName())
                .dataLink(mindmapTemplate.get().getDataLink())
                .dataNode(mindmapTemplate.get().getDataNode())
                .build();
    }

    @Override
    @Transactional
    public HashMap<String, String> updateMindMapTemplate(Long id, MindmapTemplateUpdateRequest request)
            throws Exception {
        Accounts accounts = SecurityUtils.getUser();
        Optional<Departments> departments = departmentRepository.findById(request.getDepartmentId());
        Optional<MindmapTemplate> mindmapTemplate = mindmapTemplateRepository.findById(id);

        if (departments.isEmpty()) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        if (mindmapTemplate.isEmpty()) {
            throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NOT_FOUND);
        }
        if (!request.getDepartmentName().equals(departments.get().getName())) {
            throw new ApiException(SystemErrorCode.ORGANIZATION_STRUCTURE);
        }
        if (!request.getDepartmentId()
                .equals(mindmapTemplate.get().getDepartments().getId())) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }
        if (!accounts.getRoles().getCode().equals(Role.VIEN_TRUONG.name())
                && !(accounts.getRoles().getCode().equals(Role.VIEN_PHO.name()))) {
            if (!accounts.getDepartments().getId().equals(request.getDepartmentId())) {
                throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NO_PERMISSION_TO_ACCESS);
            }
            if (!accounts.getDepartments()
                    .getId()
                    .equals(mindmapTemplate.get().getDepartments().getId())) {
                throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NO_PERMISSION_TO_ACCESS);
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
            if (Objects.nonNull(mindmapTemplate.get().getUrl())
                    && !Objects.equals(mindmapTemplate.get().getUrl(), "")) {
                minioService.deleteFile(mindmapTemplate.get().getUrl());
            }
            mindmapTemplate.get().setUrl(request.getUrl());
            mindmapTemplate.get().setUriName(FileUtils.getUriName(request.getUrl()));
        }

        if (Objects.nonNull(request.getDataLink())) {
            mindmapTemplate.get().setDataLink(request.getDataLink());
        }

        if (Objects.nonNull(request.getDataNode())) {
            mindmapTemplate.get().setDataNode(request.getDataNode());
        }

        if (!request.getName().equals(mindmapTemplate.get().getName())) {
            if (mindmapTemplateRepository.existsByNameAndDepartments_IdAndIdNot(
                    request.getName(), departments.get().getId(), id)) {
                throw new ApiException(MindmapTemplateErrorCode.MINDMAP_TEMPLATE_NAME_ALREADY_EXISTS);
            }
            mindmapTemplate.get().setName(request.getName());
        }

        var mindMap = mindmapTemplateRepository.save(mindmapTemplate.get());

        historyService.SaveHistory(
                accounts,
                UPDATE_MIND_MAP,
                ObjectTableType.MIND_MAP_TEMPLATE,
                mindMap.getId(),
                mindMap.getName(),
                IconType.MIND_MAP_TEMPLATE.name(),
                null);
        return new HashMap<>();
    }

    @Override
    public ResultUrl uploadImg(MultipartFile file) throws Exception {
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

    public void validateUrlImg(String avatarUrl, Map<String, String> errors) {
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
}
