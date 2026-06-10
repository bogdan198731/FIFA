import { isCurrentLeaderboardEntry, LeaderboardEntry } from './leaderboard.model';

describe('isCurrentLeaderboardEntry', () => {
  const entry = (userId: number | null): LeaderboardEntry => ({
    rank: 1,
    userId,
    username: 'player',
    totalPoints: 10
  });

  it('does not identify anonymous entries as the current user', () => {
    expect(isCurrentLeaderboardEntry(entry(null), null)).toBeFalse();
  });

  it('identifies only the authenticated current user', () => {
    expect(isCurrentLeaderboardEntry(entry(7), 7)).toBeTrue();
    expect(isCurrentLeaderboardEntry(entry(8), 7)).toBeFalse();
  });
});
