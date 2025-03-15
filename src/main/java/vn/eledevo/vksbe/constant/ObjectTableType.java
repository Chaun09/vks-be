package vn.eledevo.vksbe.constant;

import lombok.Getter;

@Getter
public enum ObjectTableType {
    ACCOUNT("account"),
    ACCOUNT_CASE("account_case"),
    CASE("case"),
    CASE_PERSON("case_person"),
    CASE_STATUS("case_status"),
    CASE_FLOW("case_flow"),
    CITIZEN("citizen"),
    COMPUTER("computer"),
    DEPARTMENT("department"),
    DOCUMENT("document"),
    MIND_MAP_TEMPLATE("mind_map_template"),
    ORGANIZATION("organization"),
    PROFILE("profile"),
    USB("usb");
    private final String description;

    ObjectTableType(String description) {
        this.description = description;
    }
}
