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
@Table(name = "usbs")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Usbs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String usbCode;
    String usbVendorCode;
    String name;
    String keyUsb;
    String status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;

    @OneToOne
    @JoinColumn(name = "accountId", referencedColumnName = "id")
    Accounts accounts;

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
