import { Component, inject } from '@angular/core';

import { AuthService } from '../../core/services/auth.service';
import { I18nService } from '../../core/services/i18n.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent {
  private readonly auth = inject(AuthService);
  readonly i18n = inject(I18nService);
  readonly theme = inject(ThemeService);

  readonly currentUser = this.auth.currentUser;

  setTheme(id: string): void {
    this.theme.setTheme(id);
  }

  logout(): void {
    this.auth.logout();
  }
}
