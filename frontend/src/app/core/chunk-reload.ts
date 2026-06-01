import { NavigationError } from '@angular/router';

/**
 * Resilience for stale lazy-chunk references after a redeploy.
 *
 * When a new build ships, the hash-named lazy chunks change. A browser that
 * still has the previous `index.html`/`main.js` loaded will try to fetch a
 * chunk filename that no longer exists. With the Cloudflare SPA fallback
 * (`not_found_handling: "single-page-application"`) that missing chunk comes
 * back as `index.html` (HTML, 200), so the dynamic import fails with
 * "Failed to fetch dynamically imported module".
 *
 * The fix is to reload once — a fresh load pulls the new `index.html` that
 * references the current chunk hashes. A session-scoped flag prevents a
 * reload loop if the failure is something other than a stale deploy.
 */
const RELOAD_FLAG = 'wc-chunk-reload-attempted';

export function isChunkLoadError(error: unknown): boolean {
  const message =
    typeof error === 'string'
      ? error
      : ((error as { message?: string } | null)?.message ?? '');
  return /Failed to fetch dynamically imported module|error loading dynamically imported module|Importing a module script failed|ChunkLoadError/i.test(
    message
  );
}

/** Router navigation-error handler — wired via `withNavigationErrorHandler`. */
export function handleChunkNavigationError(navError: NavigationError): void {
  if (!isChunkLoadError(navError.error)) {
    console.error('Navigation error', navError.error);
    return;
  }
  if (sessionStorage.getItem(RELOAD_FLAG)) {
    // Already reloaded once and it still failed — don't loop. Surface it.
    console.error('Stale chunk persists after reload', navError.error);
    return;
  }
  sessionStorage.setItem(RELOAD_FLAG, '1');
  location.reload();
}

/** Clear the guard after a navigation succeeds, so future deploys can reload. */
export function clearChunkReloadFlag(): void {
  sessionStorage.removeItem(RELOAD_FLAG);
}
