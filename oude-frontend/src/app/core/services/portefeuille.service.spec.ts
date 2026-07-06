import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { PortefeuilleService } from './portefeuille.service';
import { PortefeuilleResponse } from '../models/portefeuille.model';

describe('PortefeuilleService', () => {
  let service: PortefeuilleService;
  let httpMock: HttpTestingController;
  let translate: TranslateService;

  const mockResponse: PortefeuilleResponse = {
    leads: [
      {
        id: 'LEAD-001', portfolioId: 'PF-001', clientLastName: 'Dupont',
        clientFirstName: 'Jean', status: 'NEW', potentialScore: 89,
        lastSyncAt: new Date().toISOString(), countryCode: 'fr'
      }
    ],
    affaires: [],
    staleData: false,
    lastSyncAt: new Date().toISOString()
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(PortefeuilleService);
    httpMock = TestBed.inject(HttpTestingController);
    translate = TestBed.inject(TranslateService);
    translate.setTranslation('fr', {
      PORTFOLIO: {
        ERROR_STALE: 'Données temporairement indisponibles — affichage du dernier état connu',
        ERROR_LOAD: 'Erreur lors du chargement du portefeuille'
      }
    });
    translate.use('fr');
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('load() doit appeler GET /api/portfolio et peupler les signals', () => {
    service.load();

    const req = httpMock.expectOne('/api/portfolio');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(service.loading()).toBeFalse();
    expect(service.leads()).toHaveSize(1);
    expect(service.leads()[0].clientLastName).toBe('Dupont');
    expect(service.staleData()).toBeFalse();
    expect(service.error()).toBeNull();
  });

  it('load() doit afficher message données indisponibles sur erreur 503', () => {
    service.load();

    const req = httpMock.expectOne('/api/portfolio');
    req.flush('Service Unavailable', { status: 503, statusText: 'Service Unavailable' });

    expect(service.loading()).toBeFalse();
    expect(service.error()).toContain('temporairement indisponibles');
    expect(service.leads()).toHaveSize(0);
  });

  it('load() doit afficher message erreur générique sur erreur 500', () => {
    service.load();

    const req = httpMock.expectOne('/api/portfolio');
    req.flush('Internal Server Error', { status: 500, statusText: 'Internal Server Error' });

    expect(service.loading()).toBeFalse();
    expect(service.error()).toContain('Erreur lors du chargement');
  });

  it('reset() doit vider tous les signals', () => {
    service.load();
    httpMock.expectOne('/api/portfolio').flush(mockResponse);

    service.reset();

    expect(service.portefeuille()).toBeNull();
    expect(service.error()).toBeNull();
    expect(service.loading()).toBeFalse();
  });
});
