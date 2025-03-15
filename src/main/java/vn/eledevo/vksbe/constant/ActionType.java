package vn.eledevo.vksbe.constant;

import lombok.Getter;

@Getter
public enum ActionType {
    UPDATE("Cập nhật"),
    CREATE("Tạo mới"),
    DELETE("Xóa"),
    VIEW("Xem"),
    MOVE("Di chuyển"),
    RENAME("Thay đổi tên"),
    UPLOAD("Tải ảnh lên");

    private final String description;

    ActionType(String description) {
        this.description = description;
    }
}
