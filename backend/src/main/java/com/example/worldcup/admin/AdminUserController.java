package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.AdminUserResponse;
import com.example.worldcup.admin.dto.UpdateUserRoleRequest;
import com.example.worldcup.user.UserPrincipal;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin user management. Lives under {@code /api/admin/**} so
 * {@code SecurityConfig}'s {@code hasRole("ADMIN")} rule applies.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserResponse> list() {
        return adminUserService.listUsers();
    }

    @PutMapping("/{userId}/role")
    public AdminUserResponse updateRole(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long userId,
                                        @Valid @RequestBody UpdateUserRoleRequest req) {
        return adminUserService.updateRole(principal.getId(), userId, req.role());
    }
}
