package vn.eledevo.vksbe.dto.response.cases;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường không cần thiết
public class StakeHolderDto {
    private Long id;
    private String username;
    private String status;
    private String avatar;
    private String fullName;
}
