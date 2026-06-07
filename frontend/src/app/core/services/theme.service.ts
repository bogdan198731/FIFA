import { DOCUMENT } from '@angular/common';
import { Injectable, computed, inject, signal } from '@angular/core';

import { DEFAULT_THEME_ID, TEAM_THEMES, TeamTheme } from '../theme/team-themes';

const THEME_KEY = 'worldcup.team-theme';
const THEME_VARIABLES = [
  '--background', '--surface', '--surface-2', '--border', '--text', '--text-muted',
  '--accent', '--accent-strong', '--accent-soft', '--on-accent', '--success',
  '--danger', '--warning', '--gold', '--nav', '--nav-soft', '--nav-text',
  '--nav-muted', '--shadow-soft', '--shadow-card'
] as const;

type Rgb = [number, number, number];

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly _selectedId = signal(this.detectTheme());
  private colorScheme: MediaQueryList | null = null;

  readonly themes = TEAM_THEMES;
  readonly selectedId = this._selectedId.asReadonly();
  readonly selectedTheme = computed(
    () => TEAM_THEMES.find(theme => theme.id === this._selectedId()) ?? null
  );

  initialize(): void {
    this.colorScheme = globalThis.matchMedia?.('(prefers-color-scheme: dark)') ?? null;
    this.colorScheme?.addEventListener('change', this.onColorSchemeChange);
    this.apply();
  }

  setTheme(id: string): void {
    const validId = id === DEFAULT_THEME_ID || TEAM_THEMES.some(theme => theme.id === id)
      ? id
      : DEFAULT_THEME_ID;
    this._selectedId.set(validId);
    try { localStorage.setItem(THEME_KEY, validId); } catch { /* private browsing */ }
    this.apply();
  }

  private readonly onColorSchemeChange = (): void => this.apply();

  private detectTheme(): string {
    try {
      const stored = localStorage.getItem(THEME_KEY);
      if (stored && (stored === DEFAULT_THEME_ID || TEAM_THEMES.some(theme => theme.id === stored))) {
        return stored;
      }
    } catch { /* private browsing */ }
    return DEFAULT_THEME_ID;
  }

  private apply(): void {
    const root = this.document.documentElement;
    const theme = this.selectedTheme();
    if (!theme) {
      THEME_VARIABLES.forEach(variable => root.style.removeProperty(variable));
      delete root.dataset['teamTheme'];
      return;
    }

    const tokens = this.buildTokens(theme, this.colorScheme?.matches ?? false);
    Object.entries(tokens).forEach(([variable, value]) => root.style.setProperty(variable, value));
    root.dataset['teamTheme'] = theme.id;
  }

  private buildTokens(theme: TeamTheme, dark: boolean): Record<string, string> {
    const primary = normalize(theme.primary);
    const secondary = normalize(theme.secondary);
    const background = dark ? mix(primary, '#07100c', 0.86) : mix(primary, '#f6f5ef', 0.94);
    const surface = dark ? mix(primary, '#111d18', 0.82) : mix(primary, '#fffefa', 0.97);
    const surface2 = dark ? mix(primary, '#1a2a23', 0.76) : mix(primary, '#eef1eb', 0.91);
    const text = dark ? '#f1f7f3' : '#14231d';
    const textMuted = dark ? mix(primary, '#aabbb2', 0.84) : mix(primary, '#66736d', 0.86);
    const accentBase = bestAccent(primary, secondary, background, dark);
    const accentStrong = shiftForContrast(accentBase, background, 5.2, dark);
    const accentSoft = dark ? mix(accentStrong, background, 0.68) : mix(accentStrong, background, 0.84);
    const nav = mix(primary, '#08140f', dark ? 0.72 : 0.74);
    const navSoft = mix(primary, nav, 0.58);
    const navText = readableText(nav);

    return {
      '--background': background,
      '--surface': surface,
      '--surface-2': surface2,
      '--border': dark ? mix(primary, '#405047', 0.76) : mix(primary, '#dce1d7', 0.9),
      '--text': text,
      '--text-muted': textMuted,
      '--accent': accentStrong,
      '--accent-strong': shiftForContrast(accentStrong, background, 6.5, dark),
      '--accent-soft': accentSoft,
      '--on-accent': readableText(accentStrong),
      '--success': dark ? '#63d7a7' : '#087f5b',
      '--danger': dark ? '#ff8c84' : '#c2413b',
      '--warning': dark ? '#e9bd64' : '#a86f18',
      '--gold': shiftForContrast(secondary, background, 3, dark),
      '--nav': nav,
      '--nav-soft': navSoft,
      '--nav-text': navText,
      '--nav-muted': mix(navText, nav, 0.38),
      '--shadow-soft': dark ? '0 12px 32px rgba(0, 0, 0, 0.22)' : `0 10px 30px ${withAlpha(primary, 0.1)}`,
      '--shadow-card': dark ? '0 20px 48px rgba(0, 0, 0, 0.28)' : `0 18px 45px ${withAlpha(primary, 0.14)}`
    };
  }
}

function bestAccent(primary: string, secondary: string, background: string, dark: boolean): string {
  if (contrast(primary, background) >= 4.5) return primary;
  if (contrast(secondary, background) >= 4.5) return secondary;
  return shiftForContrast(primary, background, 4.5, dark);
}

function shiftForContrast(color: string, background: string, target: number, dark: boolean): string {
  let result = color;
  const destination = dark ? '#ffffff' : '#000000';
  for (let step = 0; step <= 10 && contrast(result, background) < target; step++) {
    result = mix(result, destination, 0.12);
  }
  return result;
}

function readableText(background: string): string {
  return contrast('#ffffff', background) >= contrast('#102018', background)
    ? '#ffffff'
    : '#102018';
}

function normalize(color: string): string {
  return toHex(toRgb(color));
}

function mix(a: string, b: string, weightOfB: number): string {
  const first = toRgb(a);
  const second = toRgb(b);
  return toHex(first.map((channel, index) =>
    Math.round(channel * (1 - weightOfB) + second[index] * weightOfB)
  ) as Rgb);
}

function withAlpha(color: string, alpha: number): string {
  const [r, g, b] = toRgb(color);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

function contrast(a: string, b: string): number {
  const lighter = Math.max(luminance(a), luminance(b));
  const darker = Math.min(luminance(a), luminance(b));
  return (lighter + 0.05) / (darker + 0.05);
}

function luminance(color: string): number {
  return toRgb(color)
    .map(channel => channel / 255)
    .map(channel => channel <= 0.03928 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4)
    .reduce((total, channel, index) => total + channel * [0.2126, 0.7152, 0.0722][index], 0);
}

function toRgb(color: string): Rgb {
  const value = color.replace('#', '');
  return [
    Number.parseInt(value.slice(0, 2), 16),
    Number.parseInt(value.slice(2, 4), 16),
    Number.parseInt(value.slice(4, 6), 16)
  ];
}

function toHex([r, g, b]: Rgb): string {
  return `#${[r, g, b].map(channel => channel.toString(16).padStart(2, '0')).join('')}`;
}
