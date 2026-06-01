import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { SyncService } from '../../core/services/sync.service';
import { SyncResult } from '../../core/models/sync.model';

/**
 * Reusable "update the database from the external football API" button.
 *
 * Calls {@code POST /api/admin/sync} (the API-Football sync agent), shows a
 * loading state and a result/error line, and emits {@code synced} so the host
 * page can refresh its data once the database has been updated.
 */
@Component({
  selector: 'app-external-sync-button',
  standalone: true,
  template: `
    <div class="ext-sync">
      <button type="button" class="ext-sync__btn" [disabled]="syncing()" (click)="run()">
        {{ syncing() ? 'Updating from API…' : label }}
      </button>

      @if (result(); as r) {
        <p class="ext-sync__msg ext-sync__msg--ok" aria-live="polite">
          Updated from external API — {{ r.created }} created,
          {{ r.updated }} updated, {{ r.unchanged }} unchanged.
        </p>
      } @else if (errorMessage()) {
        <p class="ext-sync__msg ext-sync__msg--err" role="alert">{{ errorMessage() }}</p>
      }
    </div>
  `,
  styles: [
    `
      .ext-sync {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.85rem;
      }
      .ext-sync__btn {
        padding: 0.55rem 1rem;
        border: 0;
        border-radius: 8px;
        background: var(--accent);
        color: var(--on-accent);
        font-weight: 600;
        cursor: pointer;
        transition: background 0.15s ease;
      }
      .ext-sync__btn:hover:not(:disabled) {
        background: var(--accent-strong);
      }
      .ext-sync__btn:disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }
      .ext-sync__msg {
        margin: 0;
        font-size: 0.9rem;
      }
      .ext-sync__msg--ok {
        color: var(--success);
      }
      .ext-sync__msg--err {
        color: var(--danger);
      }
    `
  ]
})
export class ExternalSyncButtonComponent {
  private readonly syncService = inject(SyncService);

  @Input() label = 'Update from external API';
  @Output() readonly synced = new EventEmitter<SyncResult>();

  readonly syncing = signal(false);
  readonly result = signal<SyncResult | null>(null);
  readonly errorMessage = signal<string | null>(null);

  run(): void {
    if (this.syncing()) {
      return;
    }
    this.syncing.set(true);
    this.result.set(null);
    this.errorMessage.set(null);

    this.syncService.run().subscribe({
      next: (result) => {
        this.result.set(result);
        this.syncing.set(false);
        this.synced.emit(result);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(
          err.error?.message ??
            'External API update failed. Check that API_FOOTBALL_KEY is configured.'
        );
        this.syncing.set(false);
      }
    });
  }
}
