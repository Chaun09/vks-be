package vn.eledevo.vksbe.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.SecurityUtils;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "case_person",
        indexes = {
            @Index(name = "idx_case_person_case_id", columnList = "caseId"),
            @Index(name = "idx_case_person_citizen_id", columnList = "citizenId")
        })
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CasePerson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String type;
    Boolean isDeleted = Boolean.FALSE;
    String investigatorCode;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caseId", nullable = false)
    Cases cases;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizenId", nullable = false)
    Citizens citizens;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = SecurityUtils.getUserName();
        this.updatedBy = SecurityUtils.getUserName();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = SecurityUtils.getUserName();
    }
}
