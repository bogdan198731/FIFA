/**
 * Result of a manual external-API sync (POST /api/admin/sync). Mirrors the
 * backend {@code SyncResult} record produced by {@code WorldCupSyncPersister}.
 */
export interface SyncResult {
  created: number;
  updated: number;
  unchanged: number;
}
