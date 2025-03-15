package vn.eledevo.vksbe.dto.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Result<T> {
    List<T> content;
    Integer total;
}
