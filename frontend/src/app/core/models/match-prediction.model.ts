export type MatchType = 'REGULAR' | 'KNOCKOUT';

export type MatchStage =
  | 'GROUP'
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
  locked: boolean;
}

export interface MatchPrediction {
  id: number;
  matchId: number;
  homeScore: number;
  awayScore: number;
  qualifiedTeam: string | null;
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
