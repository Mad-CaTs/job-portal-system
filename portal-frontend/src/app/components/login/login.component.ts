import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';           // Para usar *ngIf, *ngFor, etc.
import { ReactiveFormsModule } from '@angular/forms';     // Para formularios reactivos
import { FormBuilder, FormGroup, Validators } from '@angular/forms'; // Para crear formularios
import { Router } from '@angular/router';                 // Para navegar entre páginas

import { AuthService } from '../../services/auth.service'; // Servicio
import { LoginRequest } from '../../models/auth.models';   // Tipo de datos

@Component({
  selector: 'app-login', // <app-login></app-login>
  standalone: true,      // Componente independiente (no necesita módulo)
  imports: [             // Qué otros módulos necesita este componente
    CommonModule,        // Para directivas básicas (*ngIf, etc.)
    ReactiveFormsModule  // Para formularios reactivos
  ], 
  templateUrl: './login.component.html', // Archivo HTML del template
  styleUrl: './login.component.css'      // Archivo CSS de estilos
})
export class LoginComponent {
  // FormGroup: Representa todo el formulario como un grupo de campos
  loginForm: FormGroup;

  // Variables para manejar estados de la UI
  isLoading = false;  // Para mostrar "Cargando..." durante login
  errorMessage = '';  // Para mostrar errores al usuario

  constructor(
    private formBuilder: FormBuilder, // Servicio para crear formularios fácilmente
    private authService: AuthService, // Nuestro servicio de autenticación
    private router: Router            // Servicio para navegar entre páginas
  ){
    // Crear el formulario con validaciones
    this.loginForm = this.formBuilder.group({
      // Campo email: requerido y debe ser valido
      email: ['', [Validators.required, Validators.email]],
      // Campo password: requerido y minimo 4 caracteres
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  /**
  * MÉTODO QUE SE EJECUTA CUANDO SE ENVÍA EL FORMULARIO
  */
  onSubmit(): void {
    // Limpiar mensaje de error previo
    this.errorMessage = '';

    // Verificar si el formulario es valido antes de enviar
    if (this.loginForm.valid) {
      // Mostrar estado de carga
      this.isLoading = true;

      // Extraer valores del formulario y tipearlos correctamente
      const loginData: LoginRequest = {
        email: this.loginForm.value.email,
        password: this.loginForm.value.password
      };

      // Llamar al servicio de autenticacion
      this.authService.login(loginData).subscribe({
        // next: Login es exitoso
        next: (response) => {
          console.log('Login Exitoso', response);
          this.isLoading = false;

          // Navegar al dashboard o página principal 
          this.router.navigate(['/dashboard']);      
        },

        // error: Error en el login
        error: (error) => {
          console.error('Error en login:', error);
          this.errorMessage = error.message || 'Error al iniciar sesion'; 
          this.isLoading = false;
        }
      });    
    } else {
      // Si el formulario no es válido, marcar todos los campos como "touched"
      // Esto hace que se muestren los mensajes de error
      this.markFormGroupTouched();
    }
  }
  
  /**
  * MÉTODO PARA OBTENER ERRORES DE UN CAMPO ESPECÍFICO
  */
  getFieldError(fieldName: string): string {
    const field = this.loginForm.get(fieldName);

    // Si el campo no fue tocado (touched) o no tiene errores, no mostrar nada
    if (!field || !field.touched || !field.errors) {
      return '';
    }

    // Verificar que tipo de error tiene y devolver mensaje apropiado
    if (field.errors['required']) {
      return `${fieldName} es requerido`;
    }

    if (field.errors['email']) {
      return 'Email no valido';
    }

    if (field.errors['minlenght']) {
      return `${fieldName} debe tener al menos ${field.errors['minlength'].requiredLength} caracteres`;
    }

    return 'Campo invalido';
  }

  /**
  * VERIFICAR SI UN CAMPO TIENE ERRORES Y FUE TOCADO
  */
  hasFieldError(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.touched && field.errors);
  }

  /**
  * MARCAR TODOS LOS CAMPOS COMO "TOCADOS" PARA MOSTRAR ERRORES
  */
  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      if (control) {
        control.markAsTouched();
      }
    });
  }

  /**
  * MÉTODO PARA IR A LA PÁGINA DE REGISTRO
  */
  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
