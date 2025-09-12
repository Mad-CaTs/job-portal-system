import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';

export const routes: Routes = [
    // Ruta por defecto redirige a /login
    {path: '', redirectTo: '/login', pathMatch: 'full'},

    // Ruta para el componente de login
    {path: 'login', component: LoginComponent},

    // Ruta temporal para dashboard
    {path: 'dashboard', redirectTo: '/login'},

    // Ruta para imagenes no encontradas
    {path: '**', redirectTo: '/login'}
];
