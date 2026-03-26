import { isDevMode } from '@angular/core';

// In fase di sviluppo locale usa l'8080, quando Vercel compila usa il link di Render in automatico
export const API_BASE_URL = isDevMode() 
    ? 'http://localhost:8080/api' 
    : 'https://bugboard-26.onrender.com/api';
