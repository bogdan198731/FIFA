package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.AdminUserResponse;
import com.example.worldcup.common.ApiException;
import com.example.worldcup.user.Role;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock UserRepository userRepository;

    private AdminUserService service;

    @BeforeEach
    void setUp() {
        service = new AdminUserService(userRepository, new AdminProperties("admin", "secret"));
    }

    @Test
    void promotesAUserToAdmin() {
        User alice = user(2L, "alice", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));

        AdminUserResponse result = service.updateRole(1L, 2L, Role.ADMIN);

        assertThat(alice.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.role()).isEqualTo(Role.ADMIN);
        assertThat(result.bootstrapAdmin()).isFalse();
    }

    @Test
    void demotesAnAdminToUser() {
        User bob = user(3L, "bob", Role.ADMIN);
        when(userRepository.findById(3L)).thenReturn(Optional.of(bob));

        service.updateRole(1L, 3L, Role.USER);

        assertThat(bob.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void rejectsChangingYourOwnRole() {
        assertThatThrownBy(() -> service.updateRole(5L, 5L, Role.USER))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsDemotingTheBootstrapAdmin() {
        User defaultAdmin = user(9L, "admin", Role.ADMIN);
        when(userRepository.findById(9L)).thenReturn(Optional.of(defaultAdmin));

        assertThatThrownBy(() -> service.updateRole(1L, 9L, Role.USER))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(defaultAdmin.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void allowsReaffirmingBootstrapAdminAsAdmin() {
        User defaultAdmin = user(9L, "admin", Role.ADMIN);
        when(userRepository.findById(9L)).thenReturn(Optional.of(defaultAdmin));

        AdminUserResponse result = service.updateRole(1L, 9L, Role.ADMIN);

        assertThat(result.bootstrapAdmin()).isTrue();
        assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void rejectsUpdatingAMissingUser() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRole(1L, 404L, Role.ADMIN))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void flagsTheBootstrapAdminInListings() {
        User defaultAdmin = user(9L, "admin", Role.ADMIN);
        User alice = user(2L, "alice", Role.USER);
        lenient().when(userRepository.findAll()).thenReturn(java.util.List.of(defaultAdmin, alice));

        var listed = service.listUsers();

        assertThat(listed).hasSize(2);
        assertThat(listed).filteredOn(AdminUserResponse::bootstrapAdmin)
                .extracting(AdminUserResponse::username)
                .containsExactly("admin");
    }

    private static User user(Long id, String username, Role role) {
        User user = new User(username, username + "@example.com", "hash", role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
