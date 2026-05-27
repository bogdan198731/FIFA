import { Match, MatchStage, MatchType } from './match-prediction.model';

export interface RecentPredictionResult {
  predictionId: number;
  matchId: number;
  homeTeam: string;
  awayTeam: string;
  kickoffAt: string;
  stage: MatchStage;
  matchType: MatchType;
  predictedHomeScore: number;
  predictedAwayScore: number;
  predictedQualifiedTeam: string | null;
  actualHomeScore: number | null;
  actualAwayScore: number | null;
  actualQualifiedTeam: string | null;
  pointsAwarded: number;
}

export interface DashboardSummary {
  currentRank: number | null;
  totalPoints: number;
  upcomingMatches: Match[];
  recentPredictionResults: RecentPredictionResult[];
  unansweredTournamentQuestionsCount: number;
}
