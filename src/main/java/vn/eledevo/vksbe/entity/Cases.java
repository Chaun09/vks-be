package vn.eledevo.vksbe.entity;

import java.time.LocalDateTime;
import java.util.List;

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
        name = "cases",
        indexes = {
            @Index(name = "idx_cases_name", columnList = "name"),
            @Index(name = "idx_cases_code", columnList = "code"),
            @Index(name = "idx_cases_updated_at", columnList = "updatedAt"),
            @Index(name = "idx_cases_department_id", columnList = "departmentId"),
            @Index(name = "idx_cases_case_status_id", columnList = "caseStatusId")
        })
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String code;
    String name;
    String description;
    String caseType;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime actualTime;
    String createdBy;
    String updatedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departmentId", nullable = false)
    Departments departments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caseStatusId", nullable = false)
    CaseStatus case_status;

    @OneToMany(mappedBy = "cases", fetch = FetchType.LAZY)
    List<Documents> documents;

    @OneToMany(mappedBy = "cases", orphanRemoval = true, fetch = FetchType.EAGER)
    List<CaseFlow> caseFlows;

    @OneToMany(mappedBy = "cases", fetch = FetchType.EAGER)
    List<CasePerson> casePersons;

    @OneToMany(mappedBy = "cases")
    List<AccountCase> accountCases;

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
