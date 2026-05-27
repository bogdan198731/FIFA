export interface TournamentQuestion {
  id: number;
  text: string;
  deadline: string;
  points: number;
  locked: boolean;
  correctAnswer: string | null;
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
