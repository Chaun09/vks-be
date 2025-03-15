package vn.eledevo.vksbe.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseFilter<T> {
    List<T> content = new ArrayList<>();
    Integer totalRecords = 0;
    Integer pageSize = 10;
    Integer page = 1;
    Integer totalPages = 0;
    int totalAssigned = 0;

    public ResponseFilter(List<T> content, Integer totalRecords, Integer pageSize, Integer page, Integer totalPages) {
        this.content = content;
        this.totalRecords = totalRecords;
        this.pageSize = pageSize;
        this.page = page;
        this.totalPages = totalPages;
    }
}
