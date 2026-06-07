export interface TeamTheme {
  id: string;
  team: string;
  flag: string;
  primary: string;
  secondary: string;
}

export const DEFAULT_THEME_ID = 'default';

export const TEAM_THEMES: readonly TeamTheme[] = [
  { id: 'austria', team: 'Austria', flag: '🇦🇹', primary: '#d81e2f', secondary: '#ffffff' },
  { id: 'belgium', team: 'Belgium', flag: '🇧🇪', primary: '#b7192e', secondary: '#f5d319' },
  { id: 'bosnia-herzegovina', team: 'Bosnia & Herzegovina', flag: '🇧🇦', primary: '#17479e', secondary: '#f8d616' },
  { id: 'croatia', team: 'Croatia', flag: '🇭🇷', primary: '#d51f2b', secondary: '#ffffff' },
  { id: 'czech-republic', team: 'Czech Republic', flag: '🇨🇿', primary: '#c8102e', secondary: '#11457e' },
  { id: 'england', team: 'England', flag: '🏴', primary: '#f4f4f2', secondary: '#123b6d' },
  { id: 'france', team: 'France', flag: '🇫🇷', primary: '#173f8a', secondary: '#d91f32' },
  { id: 'germany', team: 'Germany', flag: '🇩🇪', primary: '#f2f2ed', secondary: '#151515' },
  { id: 'netherlands', team: 'Netherlands', flag: '🇳🇱', primary: '#f36c21', secondary: '#173f8a' },
  { id: 'norway', team: 'Norway', flag: '🇳🇴', primary: '#c8102e', secondary: '#173f8a' },
  { id: 'portugal', team: 'Portugal', flag: '🇵🇹', primary: '#b51f2f', secondary: '#0b7546' },
  { id: 'scotland', team: 'Scotland', flag: '🏴', primary: '#173f8a', secondary: '#ffffff' },
  { id: 'spain', team: 'Spain', flag: '🇪🇸', primary: '#c60b1e', secondary: '#ffc400' },
  { id: 'sweden', team: 'Sweden', flag: '🇸🇪', primary: '#1769aa', secondary: '#f8d616' },
  { id: 'switzerland', team: 'Switzerland', flag: '🇨🇭', primary: '#d52b1e', secondary: '#ffffff' },
  { id: 'turkiye', team: 'Türkiye', flag: '🇹🇷', primary: '#c8102e', secondary: '#ffffff' },
  { id: 'argentina', team: 'Argentina', flag: '🇦🇷', primary: '#75aadb', secondary: '#ffffff' },
  { id: 'brazil', team: 'Brazil', flag: '🇧🇷', primary: '#f7d417', secondary: '#1f7a45' },
  { id: 'colombia', team: 'Colombia', flag: '🇨🇴', primary: '#f4cf19', secondary: '#173f8a' },
  { id: 'ecuador', team: 'Ecuador', flag: '🇪🇨', primary: '#f4cf19', secondary: '#173f8a' },
  { id: 'paraguay', team: 'Paraguay', flag: '🇵🇾', primary: '#d52b1e', secondary: '#ffffff' },
  { id: 'uruguay', team: 'Uruguay', flag: '🇺🇾', primary: '#6cace4', secondary: '#ffffff' },
  { id: 'canada', team: 'Canada', flag: '🇨🇦', primary: '#d52b1e', secondary: '#ffffff' },
  { id: 'curacao', team: 'Curaçao', flag: '🇨🇼', primary: '#1b5eaa', secondary: '#f7d417' },
  { id: 'haiti', team: 'Haiti', flag: '🇭🇹', primary: '#1b4e9b', secondary: '#d52b1e' },
  { id: 'mexico', team: 'Mexico', flag: '🇲🇽', primary: '#146b3a', secondary: '#b51f2f' },
  { id: 'panama', team: 'Panama', flag: '🇵🇦', primary: '#d52b1e', secondary: '#ffffff' },
  { id: 'usa', team: 'USA', flag: '🇺🇸', primary: '#ffffff', secondary: '#173f8a' },
  { id: 'algeria', team: 'Algeria', flag: '🇩🇿', primary: '#f4f4ef', secondary: '#147a4c' },
  { id: 'cape-verde', team: 'Cape Verde Islands', flag: '🇨🇻', primary: '#17479e', secondary: '#ffffff' },
  { id: 'congo-dr', team: 'Congo DR', flag: '🇨🇩', primary: '#1769aa', secondary: '#d52b1e' },
  { id: 'egypt', team: 'Egypt', flag: '🇪🇬', primary: '#c8102e', secondary: '#151515' },
  { id: 'ghana', team: 'Ghana', flag: '🇬🇭', primary: '#f4f1df', secondary: '#d4af37' },
  { id: 'ivory-coast', team: 'Ivory Coast', flag: '🇨🇮', primary: '#f36c21', secondary: '#ffffff' },
  { id: 'morocco', team: 'Morocco', flag: '🇲🇦', primary: '#b51f2f', secondary: '#0b7546' },
  { id: 'senegal', team: 'Senegal', flag: '🇸🇳', primary: '#f4f1df', secondary: '#158447' },
  { id: 'south-africa', team: 'South Africa', flag: '🇿🇦', primary: '#f4cf19', secondary: '#158447' },
  { id: 'tunisia', team: 'Tunisia', flag: '🇹🇳', primary: '#d52b1e', secondary: '#ffffff' },
  { id: 'australia', team: 'Australia', flag: '🇦🇺', primary: '#f4cf19', secondary: '#146b3a' },
  { id: 'iran', team: 'Iran', flag: '🇮🇷', primary: '#f4f4ef', secondary: '#147a4c' },
  { id: 'iraq', team: 'Iraq', flag: '🇮🇶', primary: '#f4f4ef', secondary: '#147a4c' },
  { id: 'japan', team: 'Japan', flag: '🇯🇵', primary: '#173f8a', secondary: '#d52b1e' },
  { id: 'jordan', team: 'Jordan', flag: '🇯🇴', primary: '#f4f4ef', secondary: '#b51f2f' },
  { id: 'qatar', team: 'Qatar', flag: '🇶🇦', primary: '#7b1734', secondary: '#ffffff' },
  { id: 'saudi-arabia', team: 'Saudi Arabia', flag: '🇸🇦', primary: '#147a4c', secondary: '#ffffff' },
  { id: 'south-korea', team: 'South Korea', flag: '🇰🇷', primary: '#e84b58', secondary: '#173f8a' },
  { id: 'uzbekistan', team: 'Uzbekistan', flag: '🇺🇿', primary: '#1769aa', secondary: '#ffffff' },
  { id: 'new-zealand', team: 'New Zealand', flag: '🇳🇿', primary: '#151515', secondary: '#ffffff' },
] as const;
