import { TestBed } from '@angular/core/testing';

import { TEAM_THEMES } from '../theme/team-themes';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let service: ThemeService;
  const root = document.documentElement;

  beforeEach(() => {
    localStorage.removeItem('worldcup.team-theme');
    delete root.dataset['teamTheme'];
    TestBed.configureTestingModule({});
    service = TestBed.inject(ThemeService);
    service.initialize();
  });

  afterEach(() => {
    service.setTheme('default');
    localStorage.removeItem('worldcup.team-theme');
  });

  it('provides a unique theme for every listed team', () => {
    expect(TEAM_THEMES.length).toBe(48);
    expect(new Set(TEAM_THEMES.map(theme => theme.id)).size).toBe(TEAM_THEMES.length);
    expect(new Set(TEAM_THEMES.map(theme => theme.team)).size).toBe(TEAM_THEMES.length);
  });

  it('applies and persists a selected team theme', () => {
    service.setTheme('brazil');

    expect(service.selectedId()).toBe('brazil');
    expect(root.dataset['teamTheme']).toBe('brazil');
    expect(root.style.getPropertyValue('--accent')).not.toBe('');
    expect(localStorage.getItem('worldcup.team-theme')).toBe('brazil');
  });

  it('keeps text and accent colors readable for every team theme', () => {
    TEAM_THEMES.forEach(theme => {
      service.setTheme(theme.id);
      const styles = root.style;

      expect(contrast(styles.getPropertyValue('--accent'), styles.getPropertyValue('--background')))
        .withContext(`${theme.team} accent`)
        .toBeGreaterThanOrEqual(4.5);
      expect(contrast(styles.getPropertyValue('--on-accent'), styles.getPropertyValue('--accent')))
        .withContext(`${theme.team} accent text`)
        .toBeGreaterThanOrEqual(4.5);
      expect(contrast(styles.getPropertyValue('--nav-text'), styles.getPropertyValue('--nav')))
        .withContext(`${theme.team} navigation text`)
        .toBeGreaterThanOrEqual(4.5);
    });
  });

  it('resets invalid selections to the default theme', () => {
    service.setTheme('brazil');
    service.setTheme('not-a-team');

    expect(service.selectedId()).toBe('default');
    expect(root.dataset['teamTheme']).toBeUndefined();
    expect(root.style.getPropertyValue('--accent')).toBe('');
  });
});

function contrast(a: string, b: string): number {
  const lighter = Math.max(luminance(a), luminance(b));
  const darker = Math.min(luminance(a), luminance(b));
  return (lighter + 0.05) / (darker + 0.05);
}

function luminance(color: string): number {
  const value = color.replace('#', '');
  return [0, 2, 4]
    .map(index => Number.parseInt(value.slice(index, index + 2), 16) / 255)
    .map(channel => channel <= 0.03928 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4)
    .reduce((total, channel, index) => total + channel * [0.2126, 0.7152, 0.0722][index], 0);
}
