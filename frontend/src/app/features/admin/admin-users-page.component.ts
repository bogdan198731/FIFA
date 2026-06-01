import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { AdminUserService } from '../../core/services/admin-user.service';
import { AuthService } from '../../core/services/auth.service';
import { AdminUser } from '../../core/models/admin-user.model';
import { Role } from '../../core/models/auth.model';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-admin-users-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './admin-users-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminUsersPageComponent implements OnInit {
  private readonly adminUserService = inject(AdminUserService);
  private readonly auth = inject(AuthService);

  readonly users = signal<AdminUser[]>([]);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);
  readonly updatingUserId = signal<number | null>(null);

  readonly currentUserId = computed(() => this.auth.currentUser()?.id ?? null);
  readonly adminCount = computed(() => this.users().filter((u) => u.role === 'ADMIN').length);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set('loading');
    this.adminUserService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.state.set('ready');
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Could not load users.');
        this.state.set('error');
      }
    });
  }

  /** True if demoting this user would leave the pool with no admins. */
  isLastAdmin(user: AdminUser): boolean {
    return user.role === 'ADMIN' && this.adminCount() <= 1;
  }

  /** True for rows whose role the current admin isn't allowed to change. */
  isLocked(user: AdminUser): boolean {
    return (
      user.bootstrapAdmin ||
      user.id === this.currentUserId() ||
      this.isLastAdmin(user)
    );
  }

  lockReason(user: AdminUser): string {
    if (user.bootstrapAdmin) {
      return 'Default admin';
    }
    if (user.id === this.currentUserId()) {
      return 'You';
    }
    if (this.isLastAdmin(user)) {
      return 'Last admin';
    }
    return '';
  }

  toggleRole(user: AdminUser): void {
    if (this.isLocked(user) || this.updatingUserId() !== null) {
      return;
    }
    const nextRole: Role = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    this.updatingUserId.set(user.id);
    this.errorMessage.set(null);

    this.adminUserService.setRole(user.id, nextRole).subscribe({
      next: (updated) => {
        this.users.update((list) =>
          list.map((u) => (u.id === updated.id ? updated : u))
        );
        this.updatingUserId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Could not update this user.');
        this.updatingUserId.set(null);
      }
    });
  }
}
