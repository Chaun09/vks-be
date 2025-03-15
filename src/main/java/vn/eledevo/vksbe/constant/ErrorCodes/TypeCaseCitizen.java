package vn.eledevo.vksbe.constant.ErrorCodes;

public enum TypeCaseCitizen {
    INVESTIGATOR("Kiểm sát viên"),
    SUSPECT_DEFENDANT("Bị can + Bị cáo"),
    SUSPECT("Bị can"),
    DEFENDANT("Bị cáo");

    private final String description;

    TypeCaseCitizen(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
