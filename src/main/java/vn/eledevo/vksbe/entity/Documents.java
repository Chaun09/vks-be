package vn.eledevo.vksbe.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.eledevo.vksbe.utils.SecurityUtils;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "documents",
        indexes = {
            @Index(name = "idx_documents_case_id", columnList = "case_id"),
            @Index(name = "idx_documents_document_type", columnList = "document_type"),
            @Index(name = "idx_documents_parent_id", columnList = "parent_id"),
        })
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Documents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @Size(max = 1000)
    String path;

    String uriName;
    String type;
    Long size;
    String description;
    Boolean isDefault;
    String documentType;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = true)
    Cases cases;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    Documents parentId;

    @OneToMany(mappedBy = "parentId", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    List<Documents> childDocuments;

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
