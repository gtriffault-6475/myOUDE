export interface Lead {
  id: string;
  portfolioId: string;
  clientLastName: string;
  clientFirstName: string;
  clientEmail?: string;
  clientPhone?: string;
  potentialScore?: number;
  status: string;
  vehicleModel?: string;
  createdAt?: string;
  lastInteractionAt?: string;
  lastSyncAt: string;
  countryCode: string;
}

export interface Affaire {
  id: string;
  portfolioId: string;
  clientLastName: string;
  clientFirstName: string;
  vehicleModel?: string;
  financingType?: string;
  status: string;
  createdAt?: string;
  dueAt?: string;
  lastSyncAt: string;
  countryCode: string;
}

export interface PortefeuilleResponse {
  leads: Lead[];
  affaires: Affaire[];
  staleData: boolean;
  lastSyncAt: string;
}
