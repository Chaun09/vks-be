package vn.eledevo.vksbe.dto.request.computer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Trimmed
public class ComputerRequestForCreate {
    @NotBlank(message = "Tên máy tính không được để trống")
    @Size(max = 255, message = "Tên máy tính có tối đa 255 kí tự")
    @Size(min = 10, message = "Tên máy tính phải có độ dài ít nhất từ 10 ký tự.")
    String name;

    @NotBlank(message = "Mã máy tính không được để trống")
    String code;

    @NotBlank(message = "Thương hiệu máy không được để trống")
    @Size(max = 10, message = "Tên hãng máy có tối đa 10 kí tự")
    String brand;

    @NotBlank(message = "Loại máy tính không được để trống")
    String type;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 kí tự")
    String note;
}
