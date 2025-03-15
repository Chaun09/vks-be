package vn.eledevo.vksbe.service.organization;

import static vn.eledevo.vksbe.constant.ActionContent.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.constant.ErrorCodes.OrganizationErrorCode;
import vn.eledevo.vksbe.constant.ErrorCodes.SystemErrorCode;
import vn.eledevo.vksbe.constant.IconType;
import vn.eledevo.vksbe.constant.ObjectTableType;
import vn.eledevo.vksbe.constant.ResponseMessage;
import vn.eledevo.vksbe.dto.request.OrganizationSearch;
import vn.eledevo.vksbe.dto.request.organization.OrganizationRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.organization.OrganizationResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Organizations;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;
import vn.eledevo.vksbe.repository.OrganizationRepository;
import vn.eledevo.vksbe.service.histories.HistoryService;
import vn.eledevo.vksbe.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationServiceImpl implements OrganizationService {
    OrganizationRepository organizationRepository;
    HistoryService historyService;

    @Override
    public Boolean organizationNameChangeDetector(Long organizationId, String organizationName) {
        Optional<Organizations> organization = organizationRepository.findById(organizationId);
        return organization.isPresent() && organization.get().getName().equals(organizationName);
    }

    @Override
    public ResponseFilter<OrganizationResponse> getOrganizationList(
            OrganizationSearch organizationSearch, Integer page, Integer pageSize) throws ApiException {
        if (page < 1) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }
        if (pageSize < 1) {
            throw new ApiException(SystemErrorCode.BAD_REQUEST_SERVER);
        }
        if (organizationSearch.getFromDate() == null) {
            organizationSearch.setFromDate(LocalDate.of(1900, 1, 1));
        }
        if (organizationSearch.getToDate() == null) {
            organizationSearch.setToDate(LocalDate.now());
        }
        Pageable pageable =
                PageRequest.of(page - 1, pageSize, Sort.by("updatedAt").descending());
        if (organizationSearch.getFromDate().isAfter(organizationSearch.getToDate())) {
            Page<OrganizationResponse> page1 = Page.empty(pageable);
            return new ResponseFilter<>(
                    page1.getContent(),
                    (int) page1.getTotalElements(),
                    page1.getSize(),
                    page1.getNumber(),
                    page1.getTotalPages());
        }
        Page<OrganizationResponse> organizationResponsePage =
                organizationRepository.getOrganizationList(organizationSearch, pageable);
        //        historyService.SaveHistory(SecurityUtils.getUser(), GET_ORGANIZATION_LIST,
        // ObjectTableType.ORGANIZATION, 0L, ORGANIZATION, IconType.ORGANIZATION.name(), null);
        return new ResponseFilter<>(
                organizationResponsePage.getContent(),
                (int) organizationResponsePage.getTotalElements(),
                organizationResponsePage.getSize(),
                organizationResponsePage.getNumber(),
                organizationResponsePage.getTotalPages());
    }

    @Override
    public HashMap<String, String> createOrganization(OrganizationRequest organizationRequest) throws ApiException {
        HashMap<String, String> errorDetails = new HashMap<>();
        Boolean checkOrganizationExistByName = organizationRepository.existsByName(organizationRequest.getName());
        if (Boolean.TRUE.equals(checkOrganizationExistByName)) {
            errorDetails.put("name", ResponseMessage.ORGANIZATION_NAME_EXIST);
        }
        // Kiểm tra mã tổ chức có tồn tại hay không
        Boolean checkOrganizationExistByCode = organizationRepository.existsByCode(organizationRequest.getCode());
        if (Boolean.TRUE.equals(checkOrganizationExistByCode)) {
            errorDetails.put("code", ResponseMessage.ORGANIZATION_CODE_EXIST);
        }
        // Nếu có lỗi thì ném ra ApiException với các chi tiết lỗi
        if (!errorDetails.isEmpty()) {
            SystemErrorCode errorCode = SystemErrorCode.VALIDATE_FORM;
            errorCode.setResult(Optional.of(errorDetails));
            throw new ApiException(errorCode);
        }

        Organizations organization = new Organizations();
        organization.setName(organizationRequest.getName());
        organization.setCode(organizationRequest.getCode());
        organization.setAddress(organizationRequest.getAddress());
        organization.setIsDefault(false);
        organization = organizationRepository.save(organization);
        historyService.SaveHistory(
                SecurityUtils.getUser(),
                CREATE_ORGANIZATION,
                ObjectTableType.ORGANIZATION,
                organization.getId(),
                organization.getName(),
                IconType.ORGANIZATION.name(),
                null);
        return new HashMap<>();
    }

    @Override
    public Organizations updateOrganization(Long organizationId, OrganizationRequest organizationRequest)
            throws ApiException, ValidationException {
        Map<String, String> errors = new HashMap<>();
        Accounts accounts = SecurityUtils.getUser();
        Optional<Organizations> organizationOptional = organizationRepository.findById(organizationId);
        if (organizationOptional.isEmpty()) {
            throw new ApiException(OrganizationErrorCode.ORGANIZATION_NOT_FOUND);
        }

        if (Boolean.TRUE.equals(organizationOptional.get().getIsDefault())) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }

        if (!(organizationRequest.getCode().equals(organizationOptional.get().getCode()))) {
            Boolean checkOrganizationExistByCode =
                    organizationRepository.existsByCodeAndIdNot(organizationRequest.getCode(), organizationId);
            if (Boolean.TRUE.equals(checkOrganizationExistByCode)) {
                errors.put("code", ResponseMessage.ORGANIZATION_CODE_EXIST);
                throw new ValidationException(errors);
            }
        }

        if (!(organizationRequest.getName().equals(organizationOptional.get().getName()))) {
            Boolean checkOrganizationExistByName =
                    organizationRepository.existsByNameAndIdNot(organizationRequest.getName(), organizationId);
            if (Boolean.TRUE.equals(checkOrganizationExistByName)) {
                errors.put("name", ResponseMessage.ORGANIZATION_NAME_EXIST);
                throw new ValidationException(errors);
            }
        }

        organizationOptional.get().setName(organizationRequest.getName());
        organizationOptional.get().setCode(organizationRequest.getCode());
        organizationOptional.get().setAddress(organizationRequest.getAddress());
        historyService.SaveHistory(
                accounts,
                UPDATE_ORGANIZATION,
                ObjectTableType.ORGANIZATION,
                organizationId,
                organizationOptional.get().getName(),
                IconType.ORGANIZATION.name(),
                null);
        return organizationRepository.save(organizationOptional.get());
    }

    @Override
    public OrganizationResponse getOrganizationDetail(Long organizationId) throws ApiException {
        Optional<Organizations> organizationOptional = organizationRepository.findById(organizationId);
        if (organizationOptional.isEmpty()) {
            throw new ApiException(OrganizationErrorCode.ORGANIZATION_NOT_FOUND);
        }
        //        historyService.SaveHistory(SecurityUtils.getUser(), GET_ORGANIZATION_DETAIL,
        // ObjectTableType.ORGANIZATION, organizationId, organizationOptional.get().getName(),
        // IconType.ORGANIZATION.name(), null);
        return OrganizationResponse.builder()
                .id(organizationOptional.get().getId())
                .name(organizationOptional.get().getName())
                .code(organizationOptional.get().getCode())
                .address(organizationOptional.get().getAddress())
                .build();
    }

    @Override
    public HashMap<String, String> deleteOrganization(Long organizationId) throws ApiException {
        Optional<Organizations> organization = organizationRepository.findById(organizationId);
        if (organization.isEmpty()) {
            throw new ApiException(OrganizationErrorCode.ORGANIZATION_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(organization.get().getIsDefault())) {
            throw new ApiException(SystemErrorCode.INTERNAL_SERVER);
        }
        organizationRepository.deleteById(organizationId);
        historyService.SaveHistory(
                SecurityUtils.getUser(),
                DELETE_ORGANIZATION,
                ObjectTableType.ORGANIZATION,
                organizationId,
                organization.get().getName(),
                IconType.ORGANIZATION.name(),
                null);
        return new HashMap<>();
    }
}
