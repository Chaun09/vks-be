package vn.eledevo.vksbe.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
    VIEN_TRUONG("Viện trưởng"),
    VIEN_PHO("Viện phó"),
    IT_ADMIN("IT Admin"),
    TRUONG_PHONG("Trưởng phòng"),
    PHO_PHONG("Phó phòng"),
    KIEM_SAT_VIEN("Kiểm sát viên"),
    ;

    private final String name;
}
