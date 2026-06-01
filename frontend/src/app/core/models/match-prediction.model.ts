export type MatchType = 'REGULAR' | 'KNOCKOUT';
export type PredictionLockOverride = 'LOCKED' | 'UNLOCKED';

export type MatchStage =
  | 'GROUP'
  | 'ROUND_OF_32'
  | 'ROUND_OF_16'
  | 'QUARTER_FINAL'
  | 'SEMI_FINAL'
  | 'THIRD_PLACE'
  | 'FINAL';

export interface Match {
  id: number;
  homeTeam: string;
  awayTeam: string;
  kickoffAt: string;
  venue: string | null;
  matchType: MatchType;
  stage: MatchStage;
  homeScore: number | null;
  awayScore: number | null;
  knockoutWinner: string | null;
  finished: boolean;
  predictionLockOverride: PredictionLockOverride | null;
  locked: boolean;
}

export interface MatchPrediction {
  id: number;
  matchId: number;
  homeScore: number;
  awayScore: number;
  qualifiedTeam: string | null;
  pointsAwarded: number;
  locked: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PredictionRequest {
  matchId: number;
  homeScore: number;
  awayScore: number;
  qualifiedTeam?: string | null;
}

export interface UpdatePredictionRequest {
  homeScore: number;
  awayScore: number;
  qualifiedTeam?: string | null;
}

export interface AdminMatchRequest {
  homeTeam: string;
  awayTeam: string;
  kickoffTime: string;
  venue?: string | null;
  stage: MatchStage;
  matchType: MatchType;
  homeScore?: number | null;
  awayScore?: number | null;
  qualifiedTeam?: string | null;
  isFinished: boolean;
}

export interface MatchResultRequest {
  homeScore?: number | null;
  awayScore?: number | null;
  qualifiedTeam?: string | null;
  isFinished: boolean;
}

export interface ScoreRecalculationResponse {
  matchesScored: number;
  predictionsScored: number;
  answersScored: number;
  usersUpdated: number;
}
