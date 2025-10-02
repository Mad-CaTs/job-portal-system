// Request para login
export interface LoginRequest {
  email: string;
  password: string;
}

// Request para registro
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  rol: string; 
}

// Response del login
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  username: string;
  email: string;
  rol: string;
}

// Response del refresh
export interface AuthResponse {
  authenticated: boolean;
  message: string;
  accessToken: string;
  refreshToken: string;
}

// Response del registro
export interface UsuarioDTO {
  id: number;
  username: string;
  email: string;
  estado: boolean;
  rol: string;
}

// Usuario autenticado
export interface User {
  email: string;
  role: string;
  username?: string;
}