package vn.eledevo.vksbe.utils;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.eledevo.vksbe.entity.Accounts;
import vn.eledevo.vksbe.entity.Departments;
import vn.eledevo.vksbe.entity.Roles;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Accounts getUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return (Accounts) securityContext.getAuthentication().getPrincipal();
    }

    public static Long getUserId() {
        return getUser().getId();
    }

    public static String getUserName() {
        return getUser().getUsername();
    }

    public static Long getRoleId() {
        return getRole().getId();
    }

    public static Roles getRole() {
        return getUser().getRoles();
    }

    public static Long getDepartmentId() {
        return getUser().getDepartments().getId();
    }

    public static Departments getDepartments() {
        return getUser().getDepartments();
    }
}
