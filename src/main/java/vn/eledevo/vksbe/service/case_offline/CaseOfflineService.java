package vn.eledevo.vksbe.service.case_offline;

import java.util.List;

import vn.eledevo.vksbe.exception.ApiException;

public interface CaseOfflineService {

    List<String> getUriName(Long caseId) throws ApiException;
}
