import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { I18nService } from '../../core/services/i18n.service';
import { LeagueService } from '../../core/services/league.service';
import { PrivateLeague } from '../../core/models/league.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

type LoadState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-leagues-page',
  standalone: true,
  imports: [RouterLink, FormsModule, LoadingSpinnerComponent, EmptyStateComponent],
  templateUrl: './leagues-page.component.html',
})
export class LeaguesPageComponent implements OnInit {
  private readonly leagueService = inject(LeagueService);
  readonly i18n = inject(I18nService);

  readonly leagues = signal<PrivateLeague[]>([]);
  readonly state = signal<LoadState>('loading');
  readonly errorMessage = signal<string | null>(null);

  readonly showCreateForm = signal(false);
  readonly showJoinForm = signal(false);
  readonly newLeagueName = signal('');
  readonly joinCode = signal('');
  readonly formError = signal<string | null>(null);
  readonly submitting = signal(false);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.state.set('loading');
    this.leagueService.myLeagues().subscribe({
      next: (leagues) => { this.leagues.set(leagues); this.state.set('ready'); },
      error: () => { this.errorMessage.set('Could not load leagues'); this.state.set('error'); }
    });
  }

  openCreate(): void {
    this.showCreateForm.set(true);
    this.showJoinForm.set(false);
    this.formError.set(null);
    this.newLeagueName.set('');
  }

  openJoin(): void {
    this.showJoinForm.set(true);
    this.showCreateForm.set(false);
    this.formError.set(null);
    this.joinCode.set('');
  }

  cancelForms(): void {
    this.showCreateForm.set(false);
    this.showJoinForm.set(false);
    this.formError.set(null);
  }

  submitCreate(): void {
    const name = this.newLeagueName().trim();
    if (!name) { this.formError.set('League name is required'); return; }
    this.submitting.set(true);
    this.formError.set(null);
    this.leagueService.create(name).subscribe({
      next: (league) => {
        this.leagues.update(list => [league, ...list]);
        this.showCreateForm.set(false);
        this.submitting.set(false);
      },
      error: (err) => {
        this.formError.set(err?.error?.message ?? 'Could not create league');
        this.submitting.set(false);
      }
    });
  }

  submitJoin(): void {
    const code = this.joinCode().trim().toUpperCase();
    if (!code) { this.formError.set('Invite code is required'); return; }
    this.submitting.set(true);
    this.formError.set(null);
    this.leagueService.join(code).subscribe({
      next: (league) => {
        this.leagues.update(list => [league, ...list]);
        this.showJoinForm.set(false);
        this.submitting.set(false);
      },
      error: (err) => {
        this.formError.set(err?.error?.message ?? 'Could not join league');
        this.submitting.set(false);
      }
    });
  }

  copyCode(code: string): void {
    navigator.clipboard.writeText(code);
  }
}
