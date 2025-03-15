package vn.eledevo.vksbe.entity;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "account_case",
        indexes = {
            @Index(name = "idx_account_case_cases_id", columnList = "caseId"),
            @Index(name = "idx_account_case_accounts_id", columnList = "accountId"),
            @Index(name = "idx_account_case_has_access", columnList = "hasAccess")
        })
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Boolean hasAccess;
    Boolean isProsecutor;
    Boolean isInCharge;
    Boolean hasPermissionDownload = Boolean.FALSE;
    String accountRole;

    @ManyToOne
    @JoinColumn(name = "accountId", nullable = false)
    Accounts accounts;

    @ManyToOne
    @JoinColumn(name = "caseId", nullable = false)
    Cases cases;
}
