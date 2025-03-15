package vn.eledevo.vksbe.constant;

import lombok.Getter;

@Getter
public class RoleCodes {
    public static final String VIEN_TRUONG = "VIEN_TRUONG";
    public static final String IT_ADMIN = "IT_ADMIN";
    public static final String VIEN_PHO = "VIEN_PHO";
    public static final String TRUONG_PHONG = "TRUONG_PHONG";
    public static final String PHO_PHONG = "PHO_PHONG";
    public static final String KIEM_SAT_VIEN = "KIEM_SAT_VIEN";

    private RoleCodes() {
        // Prevents instantiation
    }
}
