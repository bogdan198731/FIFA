import { Component, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { clearChunkReloadFlag } from './core/chunk-reload';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MainLayoutComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly router = inject(Router);

  constructor() {
    // A successful navigation means the current chunks loaded fine, so reset
    // the stale-chunk reload guard for the next deploy.
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => clearChunkReloadFlag());
  }
}
