package vn.eledevo.vksbe.service.cases;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import vn.eledevo.vksbe.constant.CasePosition;
import vn.eledevo.vksbe.dto.model.account.AccountDownloadResponse;
import vn.eledevo.vksbe.dto.request.cases.*;
import vn.eledevo.vksbe.dto.request.cases.CaseCreateRequest;
import vn.eledevo.vksbe.dto.request.cases.CaseUpdateRequest;
import vn.eledevo.vksbe.dto.response.*;
import vn.eledevo.vksbe.dto.response.account.StakeHolderResponse;
import vn.eledevo.vksbe.dto.response.account_case.AccountDownloadCaseResponse;
import vn.eledevo.vksbe.dto.response.citizen.CitizenCaseResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.exception.ApiException;
import vn.eledevo.vksbe.exception.ValidationException;

public interface CaseService {
    ResponseFilter<CitizenCaseResponse> getAllInvestigatorByCaseId(Long id, String textSearch, int page, int pageSize)
            throws ApiException;

    Long createCase(CaseCreateRequest caseCreateRequest);

    ResponseFilter<Page> getAllStakeHolderByCaseId(Long id, int page, int pageSize) throws ApiException;

    HashMap<String, String> updateCase(Long id, CaseUpdateRequest request) throws ApiException, ValidationException;

    ResponseFilter<CitizenCaseResponse> getAllSuspectDefendantByCaseId(
            Long id, String textSearch, int page, int pageSize) throws ApiException;

    HashMap<String, String> updateInvestigator(Long id, CaseCitizenUpdateRequest request) throws ApiException;

    HashMap<String, String> updateProsecutorList(CasePosition type, Long id, List<CaseAccountUpdateRequest> casePersons)
            throws ApiException;

    HashMap<String, String> updateSuspectAndDefendant(Long id, CaseCitizenUpdateRequest request) throws ApiException;

    HashMap<String, String> updateTypeCasePerson(Long id, CaseCitizenUpdateRequest request) throws ApiException;

    CaseMindmapTemplateResponse<MindmapTemplateResponse> getAllMindMapTemplates(
            Long caseId, Integer page, Integer pageSize, String textSearch) throws ApiException;

    ResultList<AccountDownloadResponse> getListAccountNoPermissionDownload(Long id, String textSearch)
            throws ApiException;

    HashMap<String, String> removePermissionDownloadCase(Long caseId, Long accountId) throws ApiException;

    ResultList<AccountDownloadCaseResponse> grantPermissionDownloadCase(Long caseId, Set<Long> accountIds)
            throws ApiException;

    ResultList<AccountDownloadResponse> getListAccountHasPermissionDownload(Long caseId) throws ApiException;
}
