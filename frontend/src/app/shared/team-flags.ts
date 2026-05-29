const TEAM_FLAGS: Record<string, string> = {
  // UEFA (Europe)
  Austria: '🇦🇹',
  Belgium: '🇧🇪',
  'Bosnia & Herzegovina': '🇧🇦',
  Croatia: '🇭🇷',
  'Czech Republic': '🇨🇿',
  England: '🏴󠁧󠁢󠁥󠁮󠁧󠁿',
  France: '🇫🇷',
  Germany: '🇩🇪',
  Netherlands: '🇳🇱',
  Norway: '🇳🇴',
  Portugal: '🇵🇹',
  Scotland: '🏴󠁧󠁢󠁳󠁣󠁴󠁿',
  Spain: '🇪🇸',
  Sweden: '🇸🇪',
  Switzerland: '🇨🇭',
  Türkiye: '🇹🇷',

  // CONMEBOL (South America)
  Argentina: '🇦🇷',
  Brazil: '🇧🇷',
  Colombia: '🇨🇴',
  Ecuador: '🇪🇨',
  Paraguay: '🇵🇾',
  Uruguay: '🇺🇾',

  // CONCACAF (North & Central America)
  Canada: '🇨🇦',
  Curaçao: '🇨🇼',
  Haiti: '🇭🇹',
  Mexico: '🇲🇽',
  Panama: '🇵🇦',
  USA: '🇺🇸',

  // CAF (Africa)
  Algeria: '🇩🇿',
  'Cape Verde Islands': '🇨🇻',
  'Congo DR': '🇨🇩',
  Egypt: '🇪🇬',
  Ghana: '🇬🇭',
  'Ivory Coast': '🇨🇮',
  Morocco: '🇲🇦',
  Senegal: '🇸🇳',
  'South Africa': '🇿🇦',
  Tunisia: '🇹🇳',

  // AFC (Asia)
  Australia: '🇦🇺',
  Iran: '🇮🇷',
  Iraq: '🇮🇶',
  Japan: '🇯🇵',
  Jordan: '🇯🇴',
  Qatar: '🇶🇦',
  'Saudi Arabia': '🇸🇦',
  'South Korea': '🇰🇷',
  Uzbekistan: '🇺🇿',

  // OFC (Oceania)
  'New Zealand': '🇳🇿',
};

export function teamFlag(team: string): string {
  return TEAM_FLAGS[team] ?? '';
}
