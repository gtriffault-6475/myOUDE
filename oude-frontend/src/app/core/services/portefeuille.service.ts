import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PortefeuilleResponse, Lead, Affaire } from '../models/portefeuille.model';

@Injectable({ providedIn: 'root' })
export class PortefeuilleService {
  private readonly _portefeuille = signal<PortefeuilleResponse | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly portefeuille = this._portefeuille.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly donneesDatees = computed(() => this._portefeuille()?.donneesDatees ?? false);
  readonly leads = computed(() => this._portefeuille()?.leads ?? []);
  readonly affaires = computed(() => this._portefeuille()?.affaires ?? []);

  constructor(private http: HttpClient) {}

  load(): void {
    this._loading.set(true);
    this._error.set(null);

    this.http.get<PortefeuilleResponse>('/api/portfolio').subscribe({
      next: (data) => {
        this._portefeuille.set(data);
        this._loading.set(false);
      },
      error: (err) => {
        this._loading.set(false);
        this._error.set(
          err.status === 503
            ? 'Données temporairement indisponibles — affichage du dernier état connu'
            : 'Erreur lors du chargement du portefeuille'
        );
      }
    });
  }

  reset(): void {
    this._portefeuille.set(null);
    this._error.set(null);
    this._loading.set(false);
  }
}
