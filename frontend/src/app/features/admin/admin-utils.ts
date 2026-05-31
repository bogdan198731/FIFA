import { Match } from '../../core/models/match-prediction.model';

export const matchStages = [
  'GROUP',
  'ROUND_OF_32',
  'ROUND_OF_16',
  'QUARTER_FINAL',
  'SEMI_FINAL',
  'THIRD_PLACE',
  'FINAL'
] as const;

export const matchTypes = ['REGULAR', 'KNOCKOUT'] as const;

export function toDateTimeLocal(iso: string): string {
  const date = new Date(iso);
  const offsetMs = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16);
}

export function fromDateTimeLocal(value: string): string {
  return new Date(value).toISOString();
}

export function lockLabel(match: Match): string {
  if (match.predictionLockOverride === 'LOCKED') {
    return 'Locked manually';
  }
  if (match.predictionLockOverride === 'UNLOCKED') {
    return 'Unlocked manually';
  }
  return match.locked ? 'Locked by kickoff' : 'Open';
}
