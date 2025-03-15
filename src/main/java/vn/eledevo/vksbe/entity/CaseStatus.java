package vn.eledevo.vksbe.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        name = "case_status",
        indexes = {
            @Index(name = "idx_case_status_id", columnList = "id"),
            @Index(name = "idx_case_status_code", columnList = "code")
        })
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @Column(unique = true, nullable = false)
    String code;

    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;
    Boolean isDefault;

    @OneToMany(mappedBy = "case_status", fetch = FetchType.LAZY)
    List<Cases> cases;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = SecurityUtils.getUserName();
        this.updatedBy = SecurityUtils.getUserName();
        this.code = UUID.randomUUID().toString();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = SecurityUtils.getUserName();
    }
}
