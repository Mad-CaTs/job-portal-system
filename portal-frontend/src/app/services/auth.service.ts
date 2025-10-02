import { Injectable } from '@angular/core';                       // Decorador para servicios
import { HttpClient, HttpHeaders } from '@angular/common/http';   // Para hacer llamadas HTTP
import { Observable, BehaviorSubject, throwError } from 'rxjs';   // Para manejar streams de datos asíncronos
import { map, catchError, tap } from 'rxjs/operators';            // Operadores para transformar datos

import {
  LoginRequest,
  RegisterRequest,
  LoginResponse,
  AuthResponse,
  UsuarioDTO,
  User
} from "../models/auth.models";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly API_URL = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.checkStoredToken();
  }

  // MÉTODO LOGIN: Envía credenciales al backend y maneja la respuesta
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials, {
      withCredentials: true 
    }).pipe(
      tap(response => {
        // Cuando el login es exitoso:
        // 1. Guardar el token en localStorage del navegador
        this.setAuthToken(response.accessToken);
        // 2. Decodificar el token y extraer información del usuario
        this.extractUserFromToken(response.accessToken);    
      }),
      catchError(this.handleError)
    );
  }

  // REGISTRO DE POSTULANTE
  registerPostulante(data: RegisterRequest): Observable<UsuarioDTO> {
    const registerData = {data};
    return this.http.post<UsuarioDTO>(`${this.API_URL}/registro/postulante`, registerData).pipe(
      catchError(this.handleError)
    );
  }


  // REGISTRO DE EMPRESA
  registerEmpresa(data: RegisterRequest): Observable<UsuarioDTO> {
    const registerData = {data}
    return this.http.post<UsuarioDTO>(`${this.API_URL}/registro/empresa`, registerData).pipe(
      catchError(this.handleError)
    );
  }


  // REFRESH DEL ACCESS TOKEN
  refreshToken(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, {}, {
      withCredentials: true 
    }).pipe(
      tap(response => {
        //Actualizar el accessToken guardado
        this.setAuthToken(response.accessToken);
      }),
      catchError(this.handleError)
    );
  }


  // LOGOUT
  logout(): Observable<any> {
    return this.http.post(`${this.API_URL}/logout`, {}, {
      withCredentials: true
    }).pipe(
      tap(() => {
        //Limpiar todos los datos de autenticacion
        this.clearAuthData();
      }),
      catchError(this.handleError)
    );  
  }


  // VERIFICAR SI EL USUARIO ESTA AUTENTICADO
  isAuthenticated(): boolean {
    const token = this.getAuthToken();
    return !!token && !this.isTokenExpired(token);
  }

  // OBTENER EL USUARIO ACTUAL
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }


  // OBTENER EL TOKEN DE AUTORIZACION
  getAuthToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  ///////////////////////////////////////METODOS PRIVADOS - USO INTERNO////////////////////////////////////////////////////////

  // GUARDAR TOKEN EN LOCAL STORAGE
  private setAuthToken(token: string): void {
    localStorage.setItem('accessToken', token);
  }

  // EXTRAER INFO DEL USUARIO CON EL JWT
  private extractUserFromToken(token: string): void {
    try{
      //Un JWT tiene 3 partes separadas por puntos: header.payload.signature
      //token.split('.')[1] obtiene el payload (parte del medio)
      //atob() decodifica de Base64 a texto
      //JSON.parse() convierte texto JSON a objeto JavaScript
      const payload = JSON.parse(atob(token.split('.')[1]));

      //Crear objeto User con los datos del token
      const user: User = {
        email: payload.sub,                  
        role: payload.rol || payload.role,    
        username: payload.username
      };
      this.currentUserSubject.next(user);  

    } catch (error) {
      console.error('Error al decodificar token:', error);
      this.clearAuthData();
    }
  }


  // VERIFICAR SI HAY TOKEN GUARDADO AL INICIALIZAR
  private checkStoredToken(): void {
    const token = this.getAuthToken();

    if (token && !this.isTokenExpired(token)) {
      this.extractUserFromToken(token);
    } else {
      this.clearAuthData();
    }
  } 
  

  // VERIFICAR SI EL TOKEN ESTA EXPIRADO
  private isTokenExpired(token: string): boolean {
    try{
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000; //Convertir a milisegundos
      return Date.now() >= exp;
    } catch (error) {
      return true; //Si no se puede decodificar, considerarlo expirado
    }
  }


  // LIMPIAR DATOS DE AUTENTICACION
  private clearAuthData(): void {
    localStorage.removeItem('accessToken');
    this.currentUserSubject.next(null);
  }

  
  // MANEJO DE ERRORES HTTP
  private handleError(error : any): Observable<never> {
    let errorMessage = 'Error Desconocido';

    if (error.error instanceof ErrorEvent) {
      //Error del lado del cliente (problemas de red, etc.)
      errorMessage = `Error : ${error.error.message}`;
    } else {
      //Error del lado del servidor (400, 401, 500, etc.)
      errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
    }

    console.error('Error en AuthService:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
