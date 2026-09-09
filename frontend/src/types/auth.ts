export type Role = 'ADMIN' | 'PROJECT_MANAGER' | 'DEVELOPER' | 'VIEWER';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role?: Role;
}

export interface AuthResponse {
  accessToken: string;
  tokenType?: string;
  user: User;
}

