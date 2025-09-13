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
        this.extractUserFromToken(response.accessToken);    
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
    //POST VACIO - El refreshToken va en la cookie automaticamente
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, {}, {
      withCredentials: true //Para enviar la cookie del refreshToken
    }).pipe(
      tap(response => {
        //Actualizar el accessToken guardado
        this.setAuthToken(response.accessToken);
      }),
      catchError(this.handleError)
    );
  }

  /**
  * LOGOUT
  */
  logout(): Observable<any> {
    //POST VACIO - El refreshToken va en la cookie
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

  /**
  * VERIFICAR SI EL USUARIO ESTA AUTENTICADO
  */
  isAuthenticated(): boolean {
    const token = this.getAuthToken();
    //Retorna true solo si hay token y no esta expirado
    return !!token && !this.isTokenExpired(token);
  }

  /**
  * OBTENER EL USUARIO ACTUAL
  */
  getCurrentUser(): User | null {
    //Devuelve el valor actual del BehaviorSubject
    return this.currentUserSubject.value;
  }

  /**
  * OBTENER EL TOKEN DE AUTORIZACION
  */
  getAuthToken(): string | null {
    //localStorage es almacenamiento del navegador que persiste entre sesiones
    return localStorage.getItem('accessToken');
  }


  ///////////////////////////////////////METODOS PRIVADOS - USO INTERNO////////////////////////////////////////////////////////
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
      //Un JWT tiene 3 partes separadas por puntos: header.payload.signature
      //token.split('.')[1] obtiene el payload (parte del medio)
      //atob() decodifica de Base64 a texto
      //JSON.parse() convierte texto JSON a objeto JavaScript
      const payload = JSON.parse(atob(token.split('.')[1]));

      //Crear objeto User con los datos del token
      const user: User = {
        email: payload.sub,                   // 'sub' es el estandar JWT para el sujeto (usuario)
        role: payload.rol || payload.role,    // el backend usa 'rol'
        username: payload.username
      };

      //Notificar a todos los componentes que estan "escuchando" que hay un nuevo usuario
      this.currentUserSubject.next(user);      
    } catch (error) {
      console.error('Error al decodificar token:', error);
      //Si hay error, limpiar todo
      this.clearAuthData();
    }
  }

  /**
  * VERIFICAR SI HAY TOKEN GUARDADO AL INICIALIZAR
  */
  private checkStoredToken(): void {
    const token = this.getAuthToken();

    //Si hay token guardado y no esta expirado
    if (token && !this.isTokenExpired(token)) {
      //Extraer informacion del usuario
      this.extractUserFromToken(token);
    } else {
      //Si no hay token o esta expirado, limpiar
      this.clearAuthData();
    }
  } 
  
  /**
  * VERIFICAR SI EL TOKEN ESTA EXPIRADO
  */
  private isTokenExpired(token: string): boolean {
    try{

      //Decodificar el payload del token
      const payload = JSON.parse(atob(token.split('.')[1]));

      //'exp' es el tiempo de expiracion en segundos (estandar JWT)
      const exp = payload.exp * 1000; //Convertir a milisegundos

      //Comparar con tiempo actual
      return Date.now() >= exp;
    } catch (error) {
      //Si hay error al decodificar, considerar expirado
      return true; //Si no se puede decodificar, considerarlo expirado
    }
  }

  /**
  * LIMPIAR DATOS DE AUTENTICACION
  */
  private clearAuthData(): void {
    //Eliminar token del localStorage
    localStorage.removeItem('accessToken');
    //Notificar que no hay usuario logueado
    this.currentUserSubject.next(null);
  }

  /**
  * MANEJO DE ERRORES HTTP
  */
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

    // throwError() crea un Observable que emite un error
    // Los componentes que usen este servicio pueden capturar este error 
    return throwError(() => new Error(errorMessage));
  }
}
