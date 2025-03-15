package vn.eledevo.vksbe.service.organization;

import java.util.HashMap;

import vn.eledevo.vksbe.dto.request.OrganizationSearch;
import vn.eledevo.vksbe.dto.request.organization.OrganizationRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.organization.OrganizationResponse;
import vn.eledevo.vksbe.entity.Organizations;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;

public interface OrganizationService {
    Boolean organizationNameChangeDetector(Long organizationId, String organizationName);

    HashMap<String, String> createOrganization(OrganizationRequest organizationRequest) throws ApiException;

    Organizations updateOrganization(Long organizationId, OrganizationRequest organizationRequest)
            throws ApiException, ValidationException;

    HashMap<String, String> deleteOrganization(Long organizationId) throws ApiException;

    OrganizationResponse getOrganizationDetail(Long organizationId) throws ApiException;

    ResponseFilter<OrganizationResponse> getOrganizationList(
            OrganizationSearch organizationSearch, Integer page, Integer pageSize) throws ApiException;
}
