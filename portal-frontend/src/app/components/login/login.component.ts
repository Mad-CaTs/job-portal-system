import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';                       // Para usar *ngIf, *ngFor, etc.
import { ReactiveFormsModule } from '@angular/forms';                 // Para formularios reactivos
import { FormBuilder, FormGroup, Validators } from '@angular/forms';  // Para crear formularios
import { Router } from '@angular/router';                             // Para navegar entre páginas

import { AuthService } from '../../services/auth.service'; 
import { LoginRequest } from '../../models/auth.models';   

@Component({
  selector: 'app-login', 
  standalone: true,     
  imports: [             
    CommonModule,      
    ReactiveFormsModule  
  ], 
  templateUrl: './login.component.html', 
  styleUrl: './login.component.css'     
})

export class LoginComponent {

  loginForm: FormGroup;
  isLoading = false;  
  errorMessage = '';  

  constructor(
    private formBuilder: FormBuilder, // Servicio para crear formularios fácilmente
    private authService: AuthService, // Servicio de autenticación
    private router: Router            // Servicio para navegar entre páginas
  ){
      this.loginForm = this.formBuilder.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  // MÉTODO QUE SE EJECUTA CUANDO SE ENVÍA EL FORMULARIO
  onSubmit(): void {
    this.errorMessage = '';

    if (this.loginForm.valid) {
      this.isLoading = true;

      const loginData: LoginRequest = {
        email: this.loginForm.value.email,
        password: this.loginForm.value.password
      };

      this.authService.login(loginData).subscribe({
        next: (response) => {
          console.log('Login Exitoso', response);
          this.isLoading = false;
          this.router.navigate(['/dashboard']);      
        },

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
 
  // MÉTODO PARA OBTENER ERRORES DE UN CAMPO ESPECÍFICO
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
    if (field.errors['minlength']) {
      return `${fieldName} debe tener al menos ${field.errors['minlength'].requiredLength} caracteres`;
    }

    return 'Campo invalido';
  }

  // VERIFICAR SI UN CAMPO TIENE ERRORES Y FUE TOCADO
  hasFieldError(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.touched && field.errors);
  }

  // MARCAR TODOS LOS CAMPOS COMO "TOCADOS" PARA MOSTRAR ERRORES
  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      if (control) {
        control.markAsTouched();
      }
    });
  }

  // MÉTODO PARA IR A LA PÁGINA DE REGISTRO
  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
