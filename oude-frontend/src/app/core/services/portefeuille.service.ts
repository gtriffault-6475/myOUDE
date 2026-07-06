import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import { PortefeuilleResponse, Lead, Affaire } from '../models/portefeuille.model';

@Injectable({ providedIn: 'root' })
export class PortefeuilleService {
  private readonly _portefeuille = signal<PortefeuilleResponse | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly portefeuille = this._portefeuille.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly staleData = computed(() => this._portefeuille()?.staleData ?? false);
  readonly leads = computed(() => this._portefeuille()?.leads ?? []);
  readonly affaires = computed(() => this._portefeuille()?.affaires ?? []);

  constructor(private http: HttpClient, private translate: TranslateService) {}

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
        const key = err.status === 503 ? 'PORTFOLIO.ERROR_STALE' : 'PORTFOLIO.ERROR_LOAD';
        this._error.set(this.translate.instant(key));
      }
    });
  }

  reset(): void {
    this._portefeuille.set(null);
    this._error.set(null);
    this._loading.set(false);
  }
}
