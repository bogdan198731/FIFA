export interface LeaderboardEntry {
  rank: number;
  userId: number | null;
  username: string;
  totalPoints: number;
}

export function isCurrentLeaderboardEntry(
  entry: LeaderboardEntry,
  currentUserId: number | null
): boolean {
  return currentUserId !== null && entry.userId === currentUserId;
}
