export interface Lead {
  id: string;
  portefeuilleId: string;
  nomClient: string;
  prenomClient: string;
  emailClient?: string;
  telephoneClient?: string;
  scorePotentiel?: number;
  statut: string;
  modeleInteresse?: string;
  dateCreation?: string;
  derniereInteraction?: string;
  derniereSynchronisation: string;
}

export interface Affaire {
  id: string;
  portefeuilleId: string;
  nomClient: string;
  prenomClient: string;
  modele?: string;
  typeFinancement?: string;
  statut: string;
  dateCreation?: string;
  echeance?: string;
  derniereSynchronisation: string;
}

export interface PortefeuilleResponse {
  leads: Lead[];
  affaires: Affaire[];
  donneesDatees: boolean;
  derniereSynchronisation: string;
}
