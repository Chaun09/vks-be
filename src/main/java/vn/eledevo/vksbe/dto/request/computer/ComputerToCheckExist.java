package vn.eledevo.vksbe.dto.request.computer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.eledevo.vksbe.utils.TrimData.Trimmed;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Trimmed
public class ComputerToCheckExist {
    String computerCode;
}
