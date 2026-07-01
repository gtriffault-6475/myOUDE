import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { VendeurProfile } from '../models/vendeur.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _profile = signal<VendeurProfile | null>(null);

  readonly profile = this._profile.asReadonly();

  constructor(private http: HttpClient) {}

  get isAuthenticated(): boolean {
    return this._profile() !== null;
  }

  async loadProfile(): Promise<void> {
    try {
      const profile = await firstValueFrom(
        this.http.get<VendeurProfile>('/api/auth/me')
      );
      this._profile.set(profile);
    } catch {
      this._profile.set(null);
      this.redirectToSso();
    }
  }

  redirectToSso(): void {
    // TODO: AQ-1 — SSO_REDIRECT_URI : remplacer par l'URL réelle du SSO Renault
    // En local, le mock SSO retourne directement un token sans redirection
    const ssoUrl = `${window.location.origin}/api/auth/sso-redirect`;
    window.location.href = ssoUrl;
  }

  clearProfile(): void {
    this._profile.set(null);
    sessionStorage.removeItem('oudejwt');
  }
}
