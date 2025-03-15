package vn.eledevo.vksbe.dto.response.citizen;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CitizenCaseResponse {
    Long id;
    String type;
    String name;
    String citizenId;
    String gender;
    String profileImage;
    String workingAddress;
    String address;
    String job;
    String uriName;

    public CitizenCaseResponse(
            Long id,
            String type,
            String name,
            String citizenId,
            String gender,
            String profileImage,
            String workingAddress,
            String address,
            String job) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.citizenId = citizenId;
        this.gender = gender;
        this.profileImage = profileImage;
        this.workingAddress = workingAddress;
        this.address = address;
        this.job = job;
    }
}
