import { Injectable, computed, signal } from '@angular/core';
import { TRANSLATIONS, Lang } from '../i18n/translations';

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly _lang = signal<Lang>(this.detect());
  readonly lang = this._lang.asReadonly();

  // Reactive translation map — Signal-aware; works with OnPush components.
  readonly t = computed(() => TRANSLATIONS[this._lang()]);

  setLang(lang: Lang): void {
    this._lang.set(lang);
    try { localStorage.setItem('lang', lang); } catch { /* SSR / private browsing */ }
  }

  private detect(): Lang {
    try {
      const stored = localStorage.getItem('lang') as Lang;
      if (stored && stored in TRANSLATIONS) return stored;
    } catch { /* ignore */ }
    const browser = (navigator?.language ?? 'en').slice(0, 2).toLowerCase();
    if (browser === 'ro') return 'ro';
    if (browser === 'fr') return 'fr';
    return 'en';
  }
}
