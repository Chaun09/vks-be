package vn.eledevo.vksbe.service.case_status;

import java.util.HashMap;

import vn.eledevo.vksbe.dto.request.case_status.CaseStatusCreateRequest;
import vn.eledevo.vksbe.dto.request.case_status.CaseStatusGetRequest;
import vn.eledevo.vksbe.dto.response.ResponseFilter;
import vn.eledevo.vksbe.dto.response.case_status.CaseStatusResponse;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;

public interface CaseStatusService {
    ResponseFilter<CaseStatusResponse> getCaseStatus(
            CaseStatusGetRequest caseStatusGetRequest, Integer page, Integer pageSize) throws ApiException;

    HashMap<String, String> createCaseStatus(CaseStatusCreateRequest caseStatusCreateRequest)
            throws ApiException, ValidationException;

    HashMap<String, String> updateCaseStatus(Long id, CaseStatusCreateRequest caseStatusCreateRequest)
            throws ApiException, ValidationException;

    CaseStatusResponse getCaseStatusDetail(Long caseStatusId) throws ApiException;
}
