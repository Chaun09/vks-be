package vn.eledevo.vksbe.constant;

public enum CasePosition {
    PROSECUTOR("Kiểm sát viên"),
    INCHARGE("Lãnh đạo");

    private final String description;

    CasePosition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
