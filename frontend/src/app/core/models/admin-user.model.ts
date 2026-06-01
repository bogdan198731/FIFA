import { Role } from './auth.model';

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  role: Role;
  totalPoints: number;
  createdAt: string;
  bootstrapAdmin: boolean;
}

export interface UpdateUserRoleRequest {
  role: Role;
}

export interface AdminConfig {
  bootstrapAdminName: string | null;
  userCount: number;
  adminCount: number;
  apiFootballKeyConfigured: boolean;
}
