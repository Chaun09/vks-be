package vn.eledevo.vksbe.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import vn.eledevo.vksbe.dto.response.account.AccountResponse;
import vn.eledevo.vksbe.dto.response.account.StakeHolderResponse;
import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Profiles;
import vn.eledevo.vksbe.entity.Roles;


@Component

public class AccountMapper {
    public AccountResponse toResponse(Accounts e) {
        if (Objects.isNull(e)) {
            return null;
        }

        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setId(e.getId());
        accountResponse.setUsername(e.getUsername());
        accountResponse.setStatus(e.getStatus());
        accountResponse.setIsConditionLogin1(e.getIsConditionLogin1());
        accountResponse.setIsConditionLogin2(e.getIsConditionLogin2());
        accountResponse.setIsConnectComputer(e.getIsConnectComputer());
        accountResponse.setIsConnectUsb(e.getIsConnectUsb());
        accountResponse.setCreatedAt(e.getCreatedAt().toLocalDate());
        accountResponse.setUpdatedAt(e.getUpdatedAt().toLocalDate());
        accountResponse.setCreatedBy(e.getCreatedBy());
        accountResponse.setUpdatedBy(e.getUpdatedBy());

        return accountResponse;
    }

    public List<AccountResponse> toListResponse(List<Accounts> eList) {
        if (CollectionUtils.isEmpty(eList)) {
            return Collections.emptyList();
        }

        List<AccountResponse> list = new ArrayList<>(eList.size());
        for (Accounts accounts : eList) {
            list.add(toResponse(accounts));
        }

        return list;
    }

//    public static StakeHolderResponse mappingStakeDBToStakeRes(Page page) {
//
//        return StakeHolderResponse.builder()
//                .id(page.getFirstResult())
//                .build();
//    }


}
