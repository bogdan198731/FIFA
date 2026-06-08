export type OptionSource =
  | 'STATIC'
  | 'TEAMS'
  | 'PLAYERS'
  | 'PLAYERS_GOALKEEPERS'
  | 'PLAYERS_DEFENDERS'
  | 'PLAYERS_MIDFIELDERS'
  | 'PLAYERS_ATTACKERS';

export interface TournamentQuestion {
  id: number;
  text: string;
  deadline: string;
  points: number;
  locked: boolean;
  correctAnswer: string | null;
  options: string[] | null;
  optionSource: OptionSource;
}

export interface TournamentAnswer {
  id: number;
  questionId: number;
  answer: string;
  pointsAwarded: number;
  locked: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SubmitAnswerRequest {
  questionId: number;
  answer: string;
}

export interface UpdateAnswerRequest {
  answer: string;
}
