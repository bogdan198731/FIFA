export type Role = 'USER' | 'ADMIN';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  expiresInMs: number;
  user: UserResponse;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}
