package vn.eledevo.vksbe.dto.model.computer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComputersModel {
    @NotBlank(message = "Tên máy tính không được để trống")
    @Size(max = 255, message = "Tên máy tính có tối đa 255 kí tự")
    String name;

    @NotBlank(message = "Thương hiệu máy không được để trống")
    @Size(max = 10, message = "Tên hãng máy có tối đa 10 kí tự")
    String brand;

    @NotBlank(message = "Loại máy tính không được để trống")
    String type;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 kí tự")
    String note;
}
