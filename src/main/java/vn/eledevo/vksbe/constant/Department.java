package vn.eledevo.vksbe.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Department {
    PB_LANH_DAO,
    PB_KY_THUAT,
    PB_TRAT_TU_XA_HOI,
    PB_AN_NINH_MA_TUY,
    PB_KINH_TE_THAM_NHUNG,
    PB_DAN_SU_HANH_CHINH_KINH_DOANH,
    PB_KHIEU_NAI_TO_CAO,
    PB_TO_CHUC_CAN_BO,
    PB_THANH_TRA_KHIEU_TO,
    PB_THI_HANH_AN;
}
