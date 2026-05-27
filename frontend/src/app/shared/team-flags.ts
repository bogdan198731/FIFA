const TEAM_FLAGS: Record<string, string> = {
  Argentina: '🇦🇷',
  Australia: '🇦🇺',
  Brazil: '🇧🇷',
  France: '🇫🇷',
  Germany: '🇩🇪',
  Mexico: '🇲🇽',
  Serbia: '🇷🇸',
  Spain: '🇪🇸'
};

export function teamFlag(team: string): string {
  return TEAM_FLAGS[team] ?? '';
}
