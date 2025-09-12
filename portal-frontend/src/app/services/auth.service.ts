import { Injectable } from '@angular/core';              // Decorador para servicios
import { HttpClient, HttpHeaders } from '@angular/common/http';  // Para hacer llamadas HTTP
import { Observable, BehaviorSubject, throwError } from 'rxjs';  // Para manejar streams de datos asíncronos
import { map, catchError, tap } from 'rxjs/operators';    // Operadores para transformar datos

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
  // URL base de tu API backend
  private readonly API_URL = 'http://localhost:8080/api/auth';

  // Inicialmente no hay usuario logueado (null)
  private currentUserSubject = new BehaviorSubject<User | null>(null);

  // Observable público: Otros componentes pueden "suscribirse" para saber cuando cambia el usuario
  public currentUser$ = this.currentUserSubject.asObservable();

  //Constructor
  constructor(private http: HttpClient) {
    this.checkStoredToken();
  }

  /**
  * MÉTODO LOGIN: Envía credenciales al backend y maneja la respuesta
  */
  login(credentials: LoginRequest): Observable<LoginResponse> {

    // this.http.post hace una petición POST HTTP
    // <LoginResponse> le dice a TypeScript qué tipo de datos esperar en la respuesta
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials, {
      withCredentials: true //Permite que las cookies se envíen automáticamente
    }).pipe( // pipe() permite "encadenar" operaciones sobre el resultado

      // tap() ejecuta código sin modificar los datos
      tap(response => {

        // Cuando el login es exitoso:
        // 1. Guardar el token en localStorage del navegador
        this.setAuthToken(response.accessToken);

        // 2. Decodificar el token y extraer información del usuario
        this.extractUserFromToken(response.refreshToken);    
      }),
      // catchError() captura errores y los transforma
      catchError(this.handleError)
    );
  }

  /**
  * REGISTRO DE POSTULANTE
  */
  registerPostulante(data: RegisterRequest): Observable<UsuarioDTO> {

    // Spread operator (...) copia todos los campos de 'data' y sobrescribe 'rol'
    const registerData = {...data, rol: 'POSTULANTE'};

    // POST a /registro/postulante con los datos
    return this.http.post<UsuarioDTO>(`${this.API_URL}/registro/postulante`, registerData).pipe(
      catchError(this.handleError)
    );
  }

  /**
  * REGISTRO DE EMPRESA
  */
  registerEmpresa(data: RegisterRequest): Observable<UsuarioDTO> {

    // Spread operator (...) copia todos los campos de 'data' y sobrescribe 'rol'
    const registerData = {...data, rol: 'EMPRESA'};
    return this.http.post<UsuarioDTO>(`${this.API_URL}/registro/empresa`, registerData).pipe(
      catchError(this.handleError)
    );
  }

  /**
  * REFRESH DEL ACCESS TOKEN
  */
  refreshToken(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, {}, {
      withCredentials: true //Para enviar la cookie del refreshToken
    }).pipe(
      tap(response => {
        this.setAuthToken(response.accessToken);
      }),
      catchError(this.handleError)
    );
  }

  /**
  * LOGOUT
  */
  logout(): Observable<any> {
    return this.http.post(`${this.API_URL}/logout`, {}, {
      withCredentials: true
    }).pipe(
      tap(() => {
        this.clearAuthData();
      }),
      catchError(this.handleError)
    );  
  }

  /**
  * VERIFICAR SI EL USUARIO ESTA AUTENTICADO
  */
  isAuthenticated(): boolean {
    const token = this.getAuthToken();
    return !!token && !this.isTokenExpired(token);
  }

  /**
  * OBTENER EL USUARIO ACTUAL
  */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
  * OBTENER EL TOKEN DE AUTORIZACION
  */
  getAuthToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  /**
  * GUARDAR TOKEN EN LOCAL STORAGE
  */
  private setAuthToken(token: string): void {
    localStorage.setItem('accessToken', token);
  }

  /**
  * EXTRAER INFO DEL USUARIO CON EL JWT
  */
  private extractUserFromToken(token: string): void {
    try{
      const payload = JSON.parse(atob(token.split('.')[1]));
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

  /**
  * VERIFICAR SI HAY TOKEN GUARDADO AL INICIALIZAR
  */
  private checkStoredToken(): void {
    const token = this.getAuthToken();
    if (token && !this.isTokenExpired(token)) {
      this.extractUserFromToken(token);
    } else {
      this.clearAuthData();
    }
  } 
  
  /**
  * VERIFICAR SI EL TOKEN ESTA EXPIRADO
  */
  private isTokenExpired(token: string): boolean {
    try{
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000; //Convertir a milisegundos
      return Date.now() >= exp;
    } catch (error) {
      return true; //Si no se puede decodificar, considerarlo expirado
    }
  }

  /**
  * LIMPIAR DATOS DE AUTENTICACION
  */
  private clearAuthData(): void {
    localStorage.removeItem('accessToken');
    this.currentUserSubject.next(null);
  }

  /**
  * MANEJO DE ERRORES HTTP
  */
  private handleError(error : any): Observable<never> {
    let errorMessage = 'Error Desconocido';

    if (error.error instanceof ErrorEvent) {
      //Error del lado del cliente
      errorMessage = `Error : ${error.error.message}`;
    } else {
      //Error del lado del servidor
      errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
    }

    console.error('Error en AuthService:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
