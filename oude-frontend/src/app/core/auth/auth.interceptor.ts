import { HttpInterceptorFn } from '@angular/common/http';

// Attache le JWT sur tous les appels /api/**
// Token stocké en sessionStorage (perdu à la fermeture de l'onglet — plus sûr que localStorage)
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('oudejwt');
  if (token && req.url.startsWith('/api')) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
