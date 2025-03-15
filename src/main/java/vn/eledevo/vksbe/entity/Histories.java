package vn.eledevo.vksbe.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "histories",
        indexes = {
            @Index(name = "idx_case_id", columnList = "caseId"),
            @Index(name = "idx_staff_code", columnList = "staffCode"),
            @Index(name = "idx_full_name", columnList = "fullName"),
            @Index(name = "idx_timestamp", columnList = "timestamp")
        })
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Histories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long staffId;
    String staffCode;
    String fullName;
    String action;
    String objectType;
    Long objectId;
    String objectName;
    LocalDateTime timestamp;
    String iconType;
    Long caseId;

    @PrePersist
    private void prePersist() {
        if (timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
