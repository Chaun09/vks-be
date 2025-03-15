package vn.eledevo.vksbe.dto.response.computer;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectComputerResponse {
    Long id;
    String computerCode;
    String name;
    Boolean isConnected;
    String reason;
}
